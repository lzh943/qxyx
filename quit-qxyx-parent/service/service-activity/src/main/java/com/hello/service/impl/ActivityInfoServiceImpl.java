package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.enums.ActivityType;
import com.hello.mapper.ActivityInfoMapper;
import com.hello.model.activity.ActivityInfo;
import com.hello.model.activity.ActivityRule;
import com.hello.model.activity.ActivitySku;
import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import com.hello.model.product.SkuInfo;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.service.ActivityInfoService;
import com.hello.service.ActivityRuleService;
import com.hello.service.ActivitySkuService;
import com.hello.service.CouponInfoService;
import com.hello.vo.activity.ActivityRuleVo;
import com.hello.vo.order.CartInfoVo;
import com.hello.vo.order.OrderConfirmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo>
        implements ActivityInfoService {
    @Resource
    private ActivityRuleService activityRuleService;
    @Resource
    private ActivitySkuService activitySkuService;
    @Resource
    private CouponInfoService couponInfoService;
    @Resource
    private ProductFeignClient productFeignClient;
    /**
     * 分页查询活动列表
     */
    @Override
    public Result pageActivityInfo(Long page, Long limit) {
        Page<ActivityInfo> pageList=new Page<>(page, limit);
        QueryWrapper<ActivityInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        Page<ActivityInfo> activityInfoPage = baseMapper.selectPage(pageList, queryWrapper);
        activityInfoPage.getRecords().stream().forEach(item ->{
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return Result.ok(activityInfoPage);
    }

    /**
     * 获取活动规则
     */
    @Override
    public Result findActivityRuleList(Long id) {
        HashMap<String,Object> result=new HashMap<>();
        LambdaQueryWrapper<ActivityRule> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRule::getActivityId,id);
        List<ActivityRule> activityRuleList = activityRuleService.list(wrapper);
        result.put("activityRuleList",activityRuleList);
        LambdaQueryWrapper<ActivitySku> skuWrapper=new LambdaQueryWrapper<>();
        skuWrapper.eq(ActivitySku::getActivityId, id);
        List<Long> skuIds = activitySkuService.list(skuWrapper).stream()
                .map(item -> item.getSkuId()).collect(Collectors.toList());
        if(skuIds.size()==0){
            result.put("skuInfoList", null);
        }else {
            List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuIds);
            result.put("skuInfoList", skuInfoList);
        }
        return Result.ok(result);
    }

    /**
     * 添加活动规则
     */
    @Override
    public Result saveActivityRule(ActivityRuleVo activityRuleVo) {
        activityRuleService.remove(new LambdaQueryWrapper<ActivityRule>()
                .eq(ActivityRule::getActivityId, activityRuleVo.getActivityId()));
        activitySkuService.remove(new LambdaQueryWrapper<ActivitySku>()
                .eq(ActivitySku::getActivityId, activityRuleVo.getActivityId()));
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        ActivityInfo activityInfo = getById(activityRuleVo.getActivityId());
        for (ActivityRule activityRule:activityRuleList){
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleService.save(activityRule);
        }
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        for(ActivitySku activitySku:activitySkuList){
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuService.save(activitySku);
        }
        return Result.ok();
    }

    /**
     * 根据关键字获取sku列表，活动使用
     */
    @Override
    public Result findSkuInfoByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        if(skuInfoList.size()==0){
            return Result.ok(skuInfoList);
        }
        List<Long> skuIds= skuInfoList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<Long> existSkuIds=baseMapper.selectSkuIdListExist(skuIds);
        List<SkuInfo> findSkuInfo=new ArrayList<>();
        for (SkuInfo skuInfo:skuInfoList){
            if(!existSkuIds.contains(skuInfo.getId())){
                findSkuInfo.add(skuInfo);
            }
        }
        return Result.ok(findSkuInfo);
    }

    /**
     * 根据skuId列表获取促销信息
     */
    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        HashMap<Long,List<String>> map=new HashMap<>();
        skuIdList.forEach(skuId->{
            List<ActivityRule> activityRuleList=baseMapper.findActivityRule(skuId);
            if(!CollectionUtils.isEmpty(activityRuleList)){
                List<String> ruleList = activityRuleList.stream().map(item -> this.getRuleDesc(item))
                        .collect(Collectors.toList());
                map.put(skuId, ruleList);
            }
        });
        return map;
    }

    /**
     * 根据userId和skuId获取营销活动和优惠卷信息
     */
    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        Map<String,Object> map=new HashMap<>();
        List<ActivityRule> activityRuleList = findActivityRuleBySkuId(skuId);
        List<CouponInfo> couponInfoList=couponInfoService.findCouponInfoList(skuId,userId);
        map.put("activityRuleList", activityRuleList);
        map.put("couponInfoList", couponInfoList);
        return map;
    }
    //根据skuId获取活动规则数据
    private List<ActivityRule> findActivityRuleBySkuId(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
        for (ActivityRule activityRule:activityRuleList) {
            String ruleDesc = this.getRuleDesc(activityRule);
            activityRule.setRuleDesc(ruleDesc);
        }
        return activityRuleList;
    }
    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }
    /**
     * 获取购物车满足条件的促销与优惠券信息
     */
    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //获取购物车,根据活动规则分组,一个活动对应多个商品,一个活动也对应多个规则,得到最优规则进行封装
        List<CartInfoVo> cartInfoVoList=findCartActivityList(cartInfoList);
        //计算参与活动的优惠总金额
        BigDecimal reduceActivity = cartInfoVoList.stream()
                .filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //获取可以使用的优惠劵列表
        List<CouponInfo> couponInfoList=couponInfoService.findCartCouponInfoList(cartInfoList,userId);
        //计算使用优惠劵可以优惠的总金额
        BigDecimal reduceCoupon=new BigDecimal(0);
        if(couponInfoList.size()!=0&&!CollectionUtils.isEmpty(couponInfoList)){
            reduceCoupon = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal().intValue() == 1)//判断是否是最优选择
                    .map(couponInfo -> couponInfo.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //显示购物车内商品的原始总金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)//判断是否被选中
                .map(cartInfo -> cartInfo.getCartPrice()
                        .multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //计算优惠后的总金额
        BigDecimal totalAmount = originalTotalAmount.subtract(reduceActivity)
                .subtract(reduceCoupon);
        OrderConfirmVo orderConfirmVo=new OrderConfirmVo();
        orderConfirmVo.setCarInfoVoList(cartInfoVoList);//根据活动规则分组,封装的是最优规则
        orderConfirmVo.setCouponInfoList(couponInfoList);//获取可以使用的优惠劵列表,并默认选择最优优惠劵
        orderConfirmVo.setActivityReduceAmount(reduceActivity);//活动优惠的总金额
        orderConfirmVo.setCouponReduceAmount(reduceCoupon);//优惠劵优惠的总金额
        orderConfirmVo.setOriginalTotalAmount(originalTotalAmount);//原始总金额
        orderConfirmVo.setTotalAmount(totalAmount);//优惠后的总金额
        return orderConfirmVo;
    }
    //购物商品根据活动规则分组
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> cartInfoVoList=new ArrayList<>();
        List<Long> skuIdList = cartInfoList.stream()//获取商品id集合
                .map(item -> item.getSkuId()).collect(Collectors.toList());
        List<ActivitySku> activitySkuList=activitySkuService.selectCartActivity(skuIdList);//获取对应的活动id
        //根据活动id进行分组(一个活动对应多个商品;key为活动id,value为对应的商品id集合)
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream()
                .collect(Collectors.groupingBy(ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId,
                                Collectors.toSet())
                ));
        //获取活动对应的规则数据(key为活动id,value为活动规则数据)
        Map<Long,List<ActivityRule>> activityIdToActivityRuleListMap=new HashMap<>();
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId)
                .collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(activityIdSet)){
            LambdaQueryWrapper<ActivityRule> wrapper=new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ActivityRule::getConditionAmount,ActivityRule::getConditionNum);
            wrapper.in(ActivityRule::getActivityId,activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleService.list(wrapper);
            activityIdToActivityRuleListMap= activityRuleList.stream()
                    .collect(Collectors.groupingBy(ActivityRule::getActivityId));

        }
        Set<Long> activitySkuIdSet=new HashSet<>();//参加活动的商品id
        if(!CollectionUtils.isEmpty(activityIdToSkuIdListMap)){//参加活动购物项的封装
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                activitySkuIdSet.addAll(entry.getValue());
                List<CartInfo> currentCarInfoList = cartInfoList.stream()//当前活动对应的商品列表
                        .filter(cartInfo -> entry.getValue().contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                //计算当前活动中购物项的总金额和总数量
                BigDecimal activityTotalPrice = computeTotalAmount(currentCarInfoList);
                Integer activityTotalNum = computeCartNum(currentCarInfoList);
                //计算活动对应规则(得到对应的最优规则)
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(entry.getKey());
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();//封装的都是同一类型的规则,所以取第一个就行
                ActivityRule activityRule=null;
                if(activityType == ActivityType.FULL_REDUCTION){//满减
                    activityRule=computeFullReduction(activityTotalPrice, currentActivityRuleList);
                }else {//满量打折
                    activityRule=computeFullDiscount(activityTotalNum,
                            activityTotalPrice, currentActivityRuleList);
                }
                CartInfoVo cartInfoVo=new CartInfoVo();
                cartInfoVo.setActivityRule(activityRule);
                cartInfoVo.setCartInfoList(currentCarInfoList);
                cartInfoVoList.add(cartInfoVo);
            }
        }
        skuIdList.removeAll(activitySkuIdSet);//移除参加活动的购物项
        if(!CollectionUtils.isEmpty(skuIdList)){
            Map<Long, CartInfo> cartInfoMap = cartInfoList.stream().collect(
                    Collectors.toMap(CartInfo::getSkuId, CartInfo -> CartInfo));
            List<CartInfo> cartInfos=new ArrayList<>();//未参加活动的购物项集合
            for(Long skuId: skuIdList){
                cartInfos.add(cartInfoMap.get(skuId));
            }
            CartInfoVo cartInfoVo=new CartInfoVo();
            cartInfoVo.setActivityRule(null);
            cartInfoVo.setCartInfoList(cartInfos);
            cartInfoVoList.add(cartInfoVo);
        }
        return cartInfoVoList;
    }
    /**
     * 计算满减最优规则
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }
    /**
     * 计算满量打折最优规则
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }
    //计算总金额
    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }
    //计算总数量
    private Integer computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
