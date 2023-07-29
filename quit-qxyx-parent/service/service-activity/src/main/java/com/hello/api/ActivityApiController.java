package com.hello.api;

import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import com.hello.service.ActivityInfoService;
import com.hello.service.CouponInfoService;
import com.hello.vo.order.CartInfoVo;
import com.hello.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityApiController {
	@Resource
	private ActivityInfoService activityInfoService;
	@Resource
	private CouponInfoService couponInfoService;

	@ApiOperation(value = "根据skuId列表获取促销信息")
	@PostMapping("inner/findActivity")
	public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList) {
		return activityInfoService.findActivity(skuIdList);
	}
	@ApiOperation(value = "根据userId和skuId获取营销活动和优惠卷信息")
	@GetMapping("inner/findActivityAndCoupon/{skuId}/{userId}")
	public Map<String,Object> findActivityAndCoupon(@PathVariable Long skuId,@PathVariable Long userId){
		return activityInfoService.findActivityAndCoupon(skuId,userId);
	}
	@ApiOperation(value = "获取购物车满足条件的促销与优惠券信息")
	@PostMapping("inner/findCartActivityAndCoupon/{userId}")
	public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,
													@PathVariable Long userId) {
		return activityInfoService.findCartActivityAndCoupon(cartInfoList,userId);
	}
	@ApiOperation(value = "购物商品根据活动规则分组")
	@PostMapping("/inner/findCartActivityList")
	public List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoParamList){
		return activityInfoService.findCartActivityList(cartInfoParamList);
	}
	@ApiOperation(value = "获取购物车对应的优惠劵")
	@PostMapping("/inner/findRangeSkuIdList/{couponId}")
	public CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList,@PathVariable Long couponId){
		return couponInfoService.findRangeSkuIdList(cartInfoList,couponId);
	}
	@ApiOperation(value = "获取购物车对应的优惠劵")
	@PutMapping("/inner/updateCouponStatus/{couponId}/{userId}/{orderId}")
	public Boolean updateCouponInfoUseStatus(@PathVariable Long couponId, @PathVariable Long userId,
											 @PathVariable Long orderId){
		return couponInfoService.updateCouponInfoUseStatus(couponId,userId,orderId);
	}
}