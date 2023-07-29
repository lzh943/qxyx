package com.hello.activity;

import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import com.hello.vo.order.CartInfoVo;
import com.hello.vo.order.OrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("service-activity")
public interface ActivityFeignClient {

    //根据skuId列表获取促销信息
    @PostMapping("/api/activity/inner/findActivity")
    Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);
    //根据userId和skuId获取营销活动和优惠卷信息
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    Map<String,Object> findActivityAndCoupon(@PathVariable Long skuId, @PathVariable Long userId);
    //获取购物车满足条件的促销与优惠券信息
    @PostMapping("/api/activity/inner/findCartActivityAndCoupon/{userId}")
    OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList, @PathVariable Long userId);
    //购物商品根据活动规则分组
    @PostMapping("/api/activity/inner/findCartActivityList")
    List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoParamList);
    //获取购物车对应的优惠劵
    @PostMapping("/api/activity/inner/findRangeSkuIdList/{couponId}")
    CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList, @PathVariable Long couponId);
    //更新优惠劵的状态
    @PutMapping("/api/activity/inner/updateCouponStatus/{couponId}/{userId}/{orderId}")
    Boolean updateCouponInfoUseStatus(@PathVariable Long couponId, @PathVariable Long userId,
                                      @PathVariable Long orderId);
}