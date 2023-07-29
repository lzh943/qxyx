package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.SkuAttrValueMapper;
import com.hello.model.product.SkuAttrValue;
import com.hello.service.SkuAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue>
        implements SkuAttrValueService {
    @Override
    public List<SkuAttrValue> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuAttrValue> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SkuAttrValue::getSkuId, skuId);
        return list(wrapper);
    }
}
