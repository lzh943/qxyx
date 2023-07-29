package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.enums.CouponRangeType;
import com.hello.enums.CouponStatus;
import com.hello.mapper.CouponInfoMapper;
import com.hello.model.activity.CouponInfo;
import com.hello.model.activity.CouponRange;
import com.hello.model.activity.CouponUse;
import com.hello.model.order.CartInfo;
import com.hello.model.product.Category;
import com.hello.model.product.SkuInfo;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.service.CouponInfoService;
import com.hello.service.CouponRangeService;
import com.hello.service.CouponUseService;
import com.hello.vo.activity.CouponRuleVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo>
        implements CouponInfoService {
    @Resource
    private CouponRangeService couponRangeService;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private CouponUseService couponUseService;
    /**
     * 分页查询优惠劵
     */
    @Override
    public Result getCouponPage(Long page, Long limit) {
        Page<CouponInfo> pageList=new Page<>(page,limit);
        Page<CouponInfo> couponInfoPage = baseMapper.selectPage(pageList, null);
        couponInfoPage.getRecords().stream().forEach(item->{
            item.setCouponTypeString(item.getCouponType().getComment());
            CouponRangeType rangeType = item.getRangeType();
            if(rangeType!=null){
                item.setRangeTypeString(rangeType.getComment());
            }
        });
        return Result.ok(couponInfoPage);
    }

    /**
     * 查询优惠劵
     */
    @Override
    public Result getCouponInfo(Long id) {
        CouponInfo couponInfo= getById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        CouponRangeType rangeType = couponInfo.getRangeType();
        if(rangeType!=null){
            couponInfo.setRangeTypeString(rangeType.getComment());
        }
        return Result.ok(couponInfo);
    }

    /**
     * 查询优惠劵的规则信息
     */
    @Override
    public Result findCouponRuleList(Long id) {
        HashMap<String,Object> map=new HashMap<>();
        CouponInfo couponInfo = getById(id);
        LambdaQueryWrapper<CouponRange> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(CouponRange::getCouponId,id);
        List<Long> rangeIds = couponRangeService.list(wrapper).stream().map(it -> it.getRangeId())
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(rangeIds)){
            if(couponInfo.getRangeType()==CouponRangeType.SKU){
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIds);
                map.put("skuInfoList", skuInfoList);
            }else if(couponInfo.getRangeType()==CouponRangeType.CATEGORY){
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIds);
                map.put("categoryList",categoryList);
            }else {
                //通用
            }
        }
        return Result.ok(map);
    }

    /**
     * 新增优惠劵规则
     */
    @Override
    public Result saveCouponRule(CouponRuleVo couponRuleVo) {
        couponRangeService.remove(new LambdaQueryWrapper<CouponRange>()
                .eq(CouponRange::getCouponId, couponRuleVo.getCouponId()));
        CouponInfo couponInfo = getById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        updateById(couponInfo);
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            couponRangeService.save(couponRange);
        }
        return Result.ok();
    }

    /**
     * 根据useId和skuId获取优惠卷信息
     */
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        List<CouponInfo> couponInfoList=baseMapper.selectCouponInfoList(skuId,skuInfo.getCategoryId(),userId);
        return couponInfoList;
    }

    /**
     * 获取购物项可以使用的优惠劵(并标记最优优惠劵)
     */
    @Override
    public List<CouponInfo> findCartCouponInfoList(List<CartInfo> cartInfoList, Long userId) {
        List<CouponInfo> userToCouponInfos=baseMapper.selectCartCouponList(userId);
        //获取当前拥有的优惠劵id
        List<Long> couponIds = userToCouponInfos.stream()
                .map(couponInfo -> couponInfo.getId())
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(couponIds)){
            return new ArrayList<CouponInfo>();
        }
        //查询优惠劵对应的范围
        LambdaQueryWrapper<CouponRange> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(CouponRange::getCouponId, couponIds);
        List<CouponRange> couponRangeList = couponRangeService.list(wrapper);
        //通过对应的优惠劵id得到skuId列表(key为优惠劵id,value为skuId列表)
        Map<Long,Set<Long>> couponIdToSkuIdsMap=findSkuIdsByCouponId(couponRangeList,cartInfoList);
        BigDecimal reduceAmount=new BigDecimal(0);
        CouponInfo optimalCouponInfo=null;
        for(CouponInfo couponInfo:userToCouponInfos){//遍历该用户拥有的优惠劵
            if(couponInfo.getRangeType()==CouponRangeType.ALL){//全场通用
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);//得到商品总金额
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }else{
                Set<Long> skuIdSet = couponIdToSkuIdsMap.get(couponInfo.getId());
                //当前满足使用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream()
                        .filter(cartInfo -> skuIdSet.contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
            if(null != optimalCouponInfo) {
                optimalCouponInfo.setIsOptimal(1);
            }
        }
        return userToCouponInfos;
    }

    /**
     * 获取购物车对应的优惠劵
     */
    @Override
    public CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId) {
        CouponInfo couponInfo = getById(couponId);
        if(couponInfo==null){
            return null;
        }
        List<CouponRange> couponRangeList = couponRangeService.list(new LambdaQueryWrapper<CouponRange>()
                .eq(CouponRange::getCouponId, couponId));
        Map<Long, Set<Long>> skuIdsByCouponId = findSkuIdsByCouponId(couponRangeList, cartInfoList);
        Set<Long> skuIdList = skuIdsByCouponId.entrySet().iterator().next().getValue();
        couponInfo.setSkuIdList((new ArrayList<>(skuIdList)));
        return couponInfo;
    }

    /**
     * 更新优惠劵的状态
     */
    @Override
    public Boolean updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId) {
        CouponUse couponUse = couponUseService.getOne(new LambdaQueryWrapper<CouponUse>()
                .eq(CouponUse::getCouponId, couponId)
                .eq(CouponUse::getUserId, userId));
        couponUse.setOrderId(orderId);
        couponUse.setCouponStatus(CouponStatus.USED);
        return couponUseService.updateById(couponUse);
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
    //通过对应的优惠劵id得到skuId列表(key为优惠劵id,value为skuId列表)
    private Map<Long, Set<Long>> findSkuIdsByCouponId(List<CouponRange> couponRangeList, List<CartInfo> cartInfoList) {
        Map<Long,Set<Long>> couponIdToSkuIdsMap=new HashMap<>();
        //根据优惠劵id分组(主要是分组获取rang_id,含义随着rangType变化,可能代表分类id,也可能代表skuId)
        Map<Long, List<CouponRange>> collect = couponRangeList.stream()
                .collect(Collectors.groupingBy(couponRange -> couponRange.getCouponId()));
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator = collect.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();//优惠劵id
            List<CouponRange> rangeList = entry.getValue();//对应当前ud使用范围的集合(可能是分类id，skuId的集合,由type决定)
            Set<Long> skuIdSet=new HashSet<>();
            for (CartInfo cartInfo:cartInfoList){
                for(CouponRange couponRange:rangeList){
                    if(couponRange.getRangeType()==CouponRangeType.SKU//类型是sku可以用
                            &&couponRange.getRangeId().longValue()==cartInfo.getSkuId().longValue()){
                        skuIdSet.add(cartInfo.getSkuId());
                    } else if (couponRange.getRangeType() == CouponRangeType.CATEGORY//类型是分类下的所有sku可用
                            &&couponRange.getRangeId().longValue()==cartInfo.getCategoryId().longValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    }
                    //通用
                }
            }
            couponIdToSkuIdsMap.put(couponId, skuIdSet);
        }
        return couponIdToSkuIdsMap;
    }




}
