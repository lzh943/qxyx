package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.SkuImage;

import java.util.List;

public interface SkuImageService extends IService<SkuImage> {
    List<SkuImage> findBySkuId(Long skuId);
}
