package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.AttrGroupMapper;
import com.hello.model.product.AttrGroup;
import com.hello.result.Result;
import com.hello.service.AttrGroupService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroup> implements AttrGroupService {
    /**
     * 分页查询平台属性分组
     */
    @Override
    public Result pageAttrGroup(Long page, Long limit, String name) {
        Page<AttrGroup> pageList=new Page<>(page,limit);
        LambdaQueryWrapper<AttrGroup> wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like(AttrGroup::getName, name);
        }
        Page<AttrGroup> attrGroupPage = baseMapper.selectPage(pageList, wrapper);
        return Result.ok(attrGroupPage);
    }
}
