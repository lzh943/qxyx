package com.hello.api;

import com.hello.model.search.SkuEs;
import com.hello.result.Result;
import com.hello.service.SkuService;
import com.hello.vo.search.SkuEsQueryVo;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/search/skuEs")
public class SearchApiController {
    @Resource
    private SkuService skuService;

    @ApiOperation(value = "获取热门商品")
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList() {
        return skuService.findHotSkuList();
    }

    @ApiOperation(value = "分页查询分类中的商品数据")
    @PostMapping("/{page}/{limit}")
    public Result getSKuEsListByCategoryId(@PathVariable Integer page, @PathVariable Integer limit,
                                           @RequestBody SkuEsQueryVo skuEsQueryVo){
        Pageable pageable= PageRequest.of(page-1, limit);
        Page<SkuEs> skuEsPage=skuService.getSKuEsListByCategoryId(skuEsQueryVo,pageable);
        return Result.ok(skuEsPage);
    }
    @ApiOperation(value = "更新商品的热度")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId) {
        // 调用服务层
        skuService.incrHotScore(skuId);
        return true;
    }
}
