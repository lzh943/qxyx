package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.SkuPosterMapper;
import com.hello.model.product.SkuPoster;
import com.hello.service.SkuPosterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuPosterServiceImpl extends ServiceImpl<SkuPosterMapper, SkuPoster> implements SkuPosterService {
    @Override
    public List<SkuPoster> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuPoster> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId, skuId);
        return list(wrapper);
    }
}
