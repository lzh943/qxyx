package com.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.acl.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
