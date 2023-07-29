package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import com.hello.result.Result;
import com.hello.vo.activity.CouponRuleVo;

import java.util.List;
import java.util.Map;

public interface CouponInfoService extends IService<CouponInfo> {
    /**
     * 分页查询优惠劵列表
     * @param page
     * @param limit
     * @return
     */
    Result getCouponPage(Long page, Long limit);

    /**
     * 查询优惠劵
     * @param id
     * @return
     */
    Result getCouponInfo(Long id);

    /**
     * 查询优惠劵的规则信息
     * @param id
     * @return
     */
    Result findCouponRuleList(Long id);

    /**
     * 新增优惠劵规则
     * @param couponRuleVo
     * @return
     */
    Result saveCouponRule(CouponRuleVo couponRuleVo);

    /**
     * 根据useId和skuId获取优惠卷信息
     * @param skuId
     * @param userId
     * @return
     */
    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);

    /**
     * 获取购物项可以使用的优惠劵(并标记最优优惠劵)
     * @param cartInfoList
     * @param userId
     * @return
     */
    List<CouponInfo> findCartCouponInfoList(List<CartInfo> cartInfoList, Long userId);

    /**
     * 获取购物车对应的优惠劵
     * @param cartInfoList
     * @param couponId
     * @return
     */
    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);

    /**
     * 更新优惠劵的状态
     * @param couponId
     * @param userId
     * @param orderId
     * @return
     */
    Boolean updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId);
}
