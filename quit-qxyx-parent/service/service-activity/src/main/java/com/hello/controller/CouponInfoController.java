package com.hello.controller;

import com.hello.model.activity.CouponInfo;
import com.hello.result.Result;
import com.hello.service.CouponInfoService;
import com.hello.vo.activity.CouponRuleVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/activity/couponInfo")

public class CouponInfoController {
    @Resource
    private CouponInfoService couponInfoService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result getCouponPage(@PathVariable Long page,@PathVariable Long limit){
        return couponInfoService.getCouponPage(page,limit);
    }
    @ApiOperation(value = "新增优惠券")
    @PostMapping("save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok();
    }
    @ApiOperation(value = "查询优惠券")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        return couponInfoService.getCouponInfo(id);
    }
    @ApiOperation(value = "修改优惠券")
    @PutMapping("update")
    public Result updateById(@RequestBody CouponInfo couponInfo) {
        couponInfoService.updateById(couponInfo);
        return Result.ok();
    }
    @ApiOperation(value = "删除优惠券")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable String id) {
        couponInfoService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value="批量删除优惠券")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        couponInfoService.removeByIds(idList);
        return Result.ok();
    }
    @ApiOperation(value = "获取优惠券规则数据")
    @GetMapping("findCouponRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        return couponInfoService.findCouponRuleList(id);
    }
    @ApiOperation(value = "新增优惠劵规则")
    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        return couponInfoService.saveCouponRule(couponRuleVo);
    }
}
