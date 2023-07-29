package com.hello.service;

import com.hello.model.search.SkuEs;
import com.hello.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkuService {
    //上架
    void upperSku(Long skuId);
    //下架
    void lowerSku(Long skuId);
    //查询热门商品
    List<SkuEs> findHotSkuList();
    //分页查询分类中的商品数据
    Page<SkuEs> getSKuEsListByCategoryId(SkuEsQueryVo skuEsQueryVo, Pageable pageable);
    //更新商品的热度
    void incrHotScore(Long skuId);
}
