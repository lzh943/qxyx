package com.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.product.Attr;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttrMapper extends BaseMapper<Attr> {
}
