package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.SkuAttrValue;

import java.util.List;

public interface SkuAttrValueService extends IService<SkuAttrValue> {
    List<SkuAttrValue> findBySkuId(Long skuId);
}
