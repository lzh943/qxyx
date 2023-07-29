package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.activity.ActivityInfo;
import com.hello.model.order.CartInfo;
import com.hello.result.Result;
import com.hello.vo.activity.ActivityRuleVo;
import com.hello.vo.order.CartInfoVo;
import com.hello.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

public interface ActivityInfoService extends IService<ActivityInfo> {
    /**
     * 分页查询活动列表
     * @param page
     * @param limit
     * @return
     */
    Result pageActivityInfo(Long page, Long limit);

    /**
     * 获取活动规则
     * @param id
     * @return
     */
    Result findActivityRuleList(Long id);

    /**
     * 添加活动规则
     * @param activityRuleVo
     * @return
     */
    Result saveActivityRule(ActivityRuleVo activityRuleVo);

    /**
     * 根据关键字获取sku列表，活动使用
     * @param keyword
     * @return
     */
    Result findSkuInfoByKeyword(String keyword);

    /**
     * 根据skuId列表获取促销信息
     * @param skuIdList
     * @return
     */
    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    /**
     * 根据userId和skuId获取营销活动和优惠卷信息
     * @param skuId
     * @param userId
     * @return
     */
    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    /**
     * 获取购物车满足条件的促销与优惠券信息
     * @param cartInfoList
     * @param userId
     * @return
     */
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    /**
     * 购物商品根据活动规则分组
     * @param cartInfoParamList
     * @return
     */
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoParamList);
}
