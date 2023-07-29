package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.SkuImageMapper;
import com.hello.model.product.SkuImage;
import com.hello.service.SkuImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {
    @Override
    public List<SkuImage> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuImage> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SkuImage::getSkuId, skuId);
        return list(wrapper);
    }
}
