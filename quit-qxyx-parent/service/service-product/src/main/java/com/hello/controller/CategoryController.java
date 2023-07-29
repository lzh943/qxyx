package com.hello.controller;

import com.hello.model.product.Category;
import com.hello.result.Result;
import com.hello.service.CategoryService;
import com.hello.vo.product.CategoryQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "商品分类信息接口")
@RestController
@RequestMapping("/admin/product/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @ApiOperation(value = "分页获取商品分类信息")
    @GetMapping("/{page}/{limit}")
    public Result listCategory(@PathVariable Long page, @PathVariable Long limit,
                               CategoryQueryVo categoryQueryVo) {
        return categoryService.pageCategory(page, limit, categoryQueryVo.getName());
    }

    @ApiOperation(value = "获取单个商品分类信息")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.ok(category);
    }

    @ApiOperation(value = "新增商品分类")
    @PostMapping("save")
    public Result save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.ok();
    }

    @ApiOperation(value = "修改商品分类")
    @PutMapping("update")
    public Result updateById(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.ok();
    }

    @ApiOperation(value = "删除商品分类")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        categoryService.removeByIds(idList);
        return Result.ok();
    }

    @ApiOperation(value = "获取排序后的全部商品分类")
    @GetMapping("findAllList")
    public Result findAllList() {
        return categoryService.findAllList();
    }
}
