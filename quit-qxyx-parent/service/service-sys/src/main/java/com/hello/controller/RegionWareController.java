package com.hello.controller;

import com.hello.model.sys.RegionWare;
import com.hello.result.Result;
import com.hello.service.RegionWareService;
import com.hello.vo.sys.RegionWareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "RegionWare管理")
@RestController
@RequestMapping(value="/admin/sys/regionWare")
public class RegionWareController {
    @Resource
    private RegionWareService regionWareService;

    @ApiOperation(value = "获取开通区域列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, RegionWareQueryVo regionWareQueryVo){
        return regionWareService.pageRegionWare(page,limit,regionWareQueryVo.getKeyword());
    }
    //添加开通区域
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody RegionWare regionWare) {
        regionWareService.saveRegionWare(regionWare);
        return Result.ok();
    }
    //删除开通区域
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        regionWareService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value = "管理开通")
    @PostMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,@PathVariable Integer status) {
        regionWareService.updateStatus(id, status);
        return Result.ok();
    }
}
