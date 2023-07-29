package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.SkuPoster;

import java.util.List;

public interface SkuPosterService extends IService<SkuPoster> {
    List<SkuPoster> findBySkuId(Long skuId);
}
