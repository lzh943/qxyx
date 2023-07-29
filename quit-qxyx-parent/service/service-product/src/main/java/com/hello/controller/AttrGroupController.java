package com.hello.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hello.model.product.AttrGroup;
import com.hello.result.Result;
import com.hello.service.AttrGroupService;
import com.hello.vo.product.AttrGroupQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "平台属性分组接口")
@RestController
@RequestMapping("/admin/product/attrGroup")
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;

    @ApiOperation(value = "获取平台属性分组分页列表")
    @GetMapping("/{page}/{limit}")
    public Result listAttrGroup(@PathVariable Long page, @PathVariable Long limit,
                                AttrGroupQueryVo attrGroupQueryVo){
        return attrGroupService.pageAttrGroup(page,limit,attrGroupQueryVo.getName());
    }

    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        AttrGroup attrGroup = attrGroupService.getById(id);
        return Result.ok(attrGroup);
    }

    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody AttrGroup attrGroup) {
        attrGroupService.save(attrGroup);
        return Result.ok();
    }

    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody AttrGroup attrGroup) {
        attrGroupService.updateById(attrGroup);
        return Result.ok();
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        attrGroupService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        attrGroupService.removeByIds(idList);
        return Result.ok();
    }

    @ApiOperation(value = "获取全部属性分组")
    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(attrGroupService.list(new LambdaQueryWrapper<AttrGroup>()
                .orderByDesc(AttrGroup::getId)));
    }
}
