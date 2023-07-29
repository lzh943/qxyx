package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.AttrMapper;
import com.hello.model.product.Attr;
import com.hello.result.Result;
import com.hello.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {
    /**
     * 根据分组id获取属性列表
     */
    @Override
    public Result findListByAttrGroupId(Long attrGroupId) {
        LambdaQueryWrapper<Attr> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Attr::getAttrGroupId, attrGroupId);
        List<Attr> attrList = list(wrapper);
        return Result.ok(attrList);
    }

}
