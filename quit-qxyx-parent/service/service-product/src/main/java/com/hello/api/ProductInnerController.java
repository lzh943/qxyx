package com.hello.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hello.model.product.Category;
import com.hello.model.product.SkuInfo;
import com.hello.service.CategoryService;
import com.hello.service.SkuInfoService;
import com.hello.vo.product.SkuInfoVo;
import com.hello.vo.product.SkuStockLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductInnerController {
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private CategoryService categoryService;

    @ApiOperation(value = "根据分类id获取分类信息")
    @GetMapping("inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable("categoryId") Long categoryId) {
        return categoryService.getById(categoryId);
    }

    @ApiOperation(value = "根据skuId获取sku信息")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getById(skuId);
    }

    @ApiOperation(value = "根据skuId列表获取信息")
    @PostMapping("inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIds) {
        return skuInfoService.findSkuInfoList(skuIds);
    }

    @ApiOperation(value = "根据关键字获取商品列表")
    @GetMapping("inner/findSkuInfoByKeyword/{keyword}")
    List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword) {
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SkuInfo::getSkuName, keyword);
        List<SkuInfo> skuInfoList = skuInfoService.list(wrapper);
        return skuInfoList;
    }

    @ApiOperation(value = "批量获取分类信息")
    @PostMapping("inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIdList) {
        return categoryService.findCategoryList(categoryIdList);
    }

    @ApiOperation(value = "获取所有分类信息")
    @GetMapping("inner/findAllCategory")
    public List<Category> findAllCategory(){
        return categoryService.list();
    }

    @ApiOperation(value = "获取新人专享")
    @GetMapping("inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList() {
        return skuInfoService.findNewPersonList();
    }

    @ApiOperation(value = "根据id获取sku信息")
    @GetMapping("/inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo findSkuInfoVoBySkuId(@PathVariable Long skuId){
        return skuInfoService.getSkuInfoVo(skuId);
    }

    @ApiOperation(value = "验证和锁定库存")
    @PostMapping("/inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList,@PathVariable String orderNo){
        return skuInfoService.checkAndLock(skuStockLockVoList,orderNo);
    }

}
