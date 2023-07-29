package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.activity.ActivityFeignClient;
import com.hello.cart.CartFeignClient;
import com.hello.constant.MqConst;
import com.hello.constant.RedisConst;
import com.hello.enums.*;
import com.hello.exception.SysException;
import com.hello.mapper.OrderInfoMapper;
import com.hello.model.activity.ActivityRule;
import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import com.hello.model.order.OrderInfo;
import com.hello.model.order.OrderItem;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.OrderInfoService;
import com.hello.service.OrderItemService;
import com.hello.service.RabbitService;
import com.hello.user.UserFeignClient;
import com.hello.utils.DateUtil;
import com.hello.vo.order.CartInfoVo;
import com.hello.vo.order.OrderConfirmVo;
import com.hello.vo.order.OrderSubmitVo;
import com.hello.vo.order.OrderUserQueryVo;
import com.hello.vo.product.SkuStockLockVo;
import com.hello.vo.user.LeaderAddressVo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Resource
    UserFeignClient userFeignClient;
    @Resource
    CartFeignClient cartFeignClient;
    @Resource
    ActivityFeignClient activityFeignClient;
    @Resource
    ProductFeignClient productFeignClient;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    RabbitService rabbitService;
    @Resource
    private OrderItemService orderItemService;
    /**
     * 确认订单
     */
    @Override
    public Result confirmOrder(Long userId) {
        LeaderAddressVo userAddressByUserId = userFeignClient.getUserAddressByUserId(userId);//用户对应的团长信息(主要是地址)
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);//被选中的购物项
        String orderNo=System.currentTimeMillis()+userId.toString();//订单唯一标识
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT+orderNo, orderNo,
                24, TimeUnit.HOURS);
        //满足被选中购物项的活动和优惠劵信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartCheckedList, userId);
        orderConfirmVo.setOrderNo(orderNo);
        orderConfirmVo.setLeaderAddressVo(userAddressByUserId);
        return Result.ok(orderConfirmVo);
    }
    /**
     * 生成订单
     */
    @Override
    public Result submitOrder(OrderSubmitVo orderSubmitVo,Long userId) {
        orderSubmitVo.setUserId(userId);
        //订单不能重复提交,重复提交判断(lua脚本保证原子性)
        String orderNo = orderSubmitVo.getOrderNo();
        if(StringUtils.isEmpty(orderNo)){
            throw new SysException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";//lua脚本
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        if(!flag){//重复提交
            throw new SysException(ResultCodeEnum.REPEAT_SUBMIT);
        }
        //验证库存并且锁定库存(订单支付成功,才会真正减小库存)
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);//获取购物车中选中的购物项
        List<CartInfo> commonSkuList = cartCheckedList.stream()
                .filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode())//获取普通类型的商品
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(commonSkuList)){
            List<SkuStockLockVo>  skuStockList = commonSkuList.stream()//变成普通商品类型集合
                    .map(item -> {
                        SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                        skuStockLockVo.setSkuId(item.getSkuId());
                        skuStockLockVo.setSkuNum(item.getSkuNum());
                        return skuStockLockVo;
                    }).collect(Collectors.toList());
            Boolean isLockSuccess = productFeignClient.checkAndLock(skuStockList, orderNo);//验证库存并且锁定库存
            if(!isLockSuccess){//库存锁定失败
                throw new SysException(ResultCodeEnum.ORDER_STOCK_FALL);
            }
        }
        //下单操作
        Long orderId=saveOrder(orderSubmitVo,cartCheckedList);//生成订单
        //下单完成后,异步操作删除redis中此用户选中的购物项(rabbitmq实现)
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_DELETE_CART
                , userId);
        return Result.ok(orderId);//返回订单id
    }
    //下单操作
    @Transactional(rollbackFor = {Exception.class})
    private Long saveOrder(OrderSubmitVo orderSubmitVo, List<CartInfo> cartCheckedList) {
        if(CollectionUtils.isEmpty(cartCheckedList)){
            throw new SysException(ResultCodeEnum.DATA_ERROR);
        }
        LeaderAddressVo userAddressByUserId = userFeignClient.getUserAddressByUserId(orderSubmitVo.getUserId());//用户对应的团长信息(主要是地址)
        if(userAddressByUserId==null){
            throw new SysException(ResultCodeEnum.DATA_ERROR);
        }
        //计算金额
        Map<String, BigDecimal> activitySplitAmount= computeActivitySplitAmount(cartCheckedList);//营销活动减少的金额
        Map<String, BigDecimal> couponInfoSplitAmount = computeCouponInfoSplitAmount(cartCheckedList, orderSubmitVo.getCouponId());//优惠劵减少的金额
        List<OrderItem> orderItemList=new ArrayList<>();
        for(CartInfo cartInfo:cartCheckedList){
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType() == SkuType.COMMON.getCode()) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderSubmitVo.getLeaderId());
            //促销活动分摊金额
            BigDecimal splitActivityAmount =activitySplitAmount.get("activity:"+orderItem.getSkuId());
            if(null == splitActivityAmount) {
                splitActivityAmount = new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(splitActivityAmount);
            //优惠券分摊金额
            BigDecimal splitCouponAmount = couponInfoSplitAmount.get("coupon:"+orderItem.getSkuId());
            if(null == splitCouponAmount) {
                splitCouponAmount = new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(splitCouponAmount);
            //优惠后的总金额
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(splitActivityAmount).subtract(splitCouponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);
            orderItemList.add(orderItem);
        }
        OrderInfo order = new OrderInfo();
        order.setUserId(orderSubmitVo.getUserId());
        order.setOrderNo(orderSubmitVo.getOrderNo());
        order.setOrderStatus(OrderStatus.UNPAID);
        order.setProcessStatus(ProcessStatus.UNPAID);
        order.setCouponId(orderSubmitVo.getCouponId());
        order.setLeaderId(orderSubmitVo.getLeaderId());
        order.setLeaderName(userAddressByUserId.getLeaderName());
        order.setLeaderPhone(userAddressByUserId.getLeaderPhone());
        order.setTakeName(userAddressByUserId.getTakeName());
        order.setReceiverName(orderSubmitVo.getReceiverName());
        order.setReceiverPhone(orderSubmitVo.getReceiverPhone());
        order.setReceiverProvince(userAddressByUserId.getProvince());
        order.setReceiverCity(userAddressByUserId.getCity());
        order.setReceiverDistrict(userAddressByUserId.getDistrict());
        order.setReceiverAddress(userAddressByUserId.getDetailAddress());
        order.setWareId(cartCheckedList.get(0).getWareId());
        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartCheckedList);
        BigDecimal activityAmount = activitySplitAmount.get("activity:total");
        if(null == activityAmount) activityAmount = new BigDecimal(0);
        BigDecimal couponAmount =couponInfoSplitAmount.get("coupon:total");
        if(null == couponAmount) couponAmount = new BigDecimal(0);
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        order.setOriginalTotalAmount(originalTotalAmount);
        order.setActivityAmount(activityAmount);
        order.setCouponAmount(couponAmount);
        order.setTotalAmount(totalAmount);
        //计算团长佣金
        BigDecimal profitRate =new BigDecimal(0);//后续完善 ->orderSetService.getProfitRate();
        BigDecimal commissionAmount = order.getTotalAmount().multiply(profitRate);
        order.setCommissionAmount(commissionAmount);
        save(order);
        Long orderId = order.getId();
        //保存订单项
        for(OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItemList);
        //更新优惠劵状态
        if(null != order.getCouponId()) {
            activityFeignClient.updateCouponInfoUseStatus(order.getCouponId(), orderSubmitVo.getUserId(), order.getId());
        }
        //更新用户购物数量
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderSubmitVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartCheckedList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);
        return orderId;
    }
    //计算总金额
    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }
    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();
        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList = activityFeignClient.findCartActivityList(cartInfoParamList);
        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }
    //计算优惠劵之后的金额
    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();
        if(null == couponId) return couponInfoSplitAmountMap;
        CouponInfo couponInfo = activityFeignClient.findRangeSkuIdList(cartInfoList, couponId);//获取优惠劵
        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }
    /**
     * 获取订单详情
     */
    @Override
    public Result getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = getById(orderId);
        orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus().getComment());
        List<OrderItem> orderItemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId()));
        orderInfo.setOrderItemList(orderItemList);
        return Result.ok(orderInfo);
    }

    /**
     * 获取订单详情(远程调用使用)
     */
    @Override
    public OrderInfo getOderInfoByOrderNo(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo orderInfo = getOne(wrapper);
        return orderInfo;
    }

    /**
     * 订单已支付,更新订单状态,扣减库存(远程调用)
     */
    @Override
    public void orderPay(String orderNo) {
        OrderInfo orderInfo = getOrderInfoByOrderNo(orderNo);
        if(null == orderInfo || orderInfo.getOrderStatus() != OrderStatus.UNPAID) return;
        //更改订单状态
        this.updateOrderStatus(orderInfo.getId(),  ProcessStatus.WAITING_DELEVER);
        //扣减库存
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_MINUS_STOCK, orderNo);
    }

    /**
     * 获取用户订单分页列表
     */
    @Override
    public Result findUserOrderPage(Long page, Long limit, OrderUserQueryVo orderUserQueryVo) {
        Page<OrderInfo> pageList=new Page<>(page, limit);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId,orderUserQueryVo.getUserId());
        wrapper.eq(OrderInfo::getOrderStatus,orderUserQueryVo.getOrderStatus());
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(pageList, wrapper);
        //获取每个订单，把每个订单里面订单项查询封装
        List<OrderInfo> orderInfoList = orderInfoPage.getRecords();
        for(OrderInfo orderInfo : orderInfoList) {
            //根据订单id查询里面所有订单项列表
            List<OrderItem> orderItemList = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, orderInfo.getId())
            );
            //把订单项集合封装到每个订单里面
            orderInfo.setOrderItemList(orderItemList);
            //封装订单状态名称
            orderInfo.getParam().put("orderStatusName",orderInfo.getOrderStatus().getComment());
        }
        return Result.ok(orderInfoPage);
    }

    //根据唯一标识获取订单信息
    private OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo =getOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus().getComment());
        List<OrderItem> orderItemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId()));
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }
    //更改订单状态
    private void updateOrderStatus(Long id, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(id);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        if(processStatus == ProcessStatus.WAITING_DELEVER) {
            orderInfo.setPaymentTime(new Date());
        } else if(processStatus == ProcessStatus.WAITING_LEADER_TAKE) {
            orderInfo.setDeliveryTime(new Date());
        } else if(processStatus == ProcessStatus.WAITING_USER_TAKE) {
            orderInfo.setTakeTime(new Date());
        }
        updateById(orderInfo);
    }

}
