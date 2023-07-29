package com.hello.controller;

import com.hello.auth.AuthContextHolder;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.search.SkuFeignClient;
import com.hello.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "商品分类")
@RestController
@RequestMapping("/api/home")
public class CategoryApiController {
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private SkuFeignClient skuFeignClient;

    @ApiOperation(value = "获取所有分类信息")
    @GetMapping("/category")
    public Result index() {
        return Result.ok(productFeignClient.findAllCategory());
    }

    @ApiOperation(value = "分页查询分类中的商品数据")
    @GetMapping("/sku/{page}/{limit}")
    public Result getSKuEsListByCategoryId(@PathVariable Integer page, @PathVariable Integer limit,
                                            SkuEsQueryVo skuEsQueryVo){
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());
        return skuFeignClient.getSKuEsListByCategoryId(page, limit, skuEsQueryVo);
    }

}
