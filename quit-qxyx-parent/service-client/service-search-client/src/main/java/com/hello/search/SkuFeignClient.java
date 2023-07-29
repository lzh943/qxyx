package com.hello.search;

import com.hello.result.Result;
import com.hello.model.search.SkuEs;
import com.hello.vo.search.SkuEsQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-search")
public interface SkuFeignClient {

    @GetMapping("/api/search/skuEs/inner/findHotSkuList")
    List<SkuEs> findHotSkuList();

    @PostMapping("/api/search/skuEs/{page}/{limit}")
    Result getSKuEsListByCategoryId(@PathVariable Integer page, @PathVariable Integer limit,
                                    @RequestBody SkuEsQueryVo skuEsQueryVo);

    @GetMapping("/api/search/skuEs/inner/incrHotScore/{skuId}")
    Boolean incrHotScore(@PathVariable("skuId") Long skuId);
}
