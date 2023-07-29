package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.RolePermissionMapper;
import com.hello.model.acl.RolePermission;
import com.hello.service.RolePermissionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements RolePermissionService {
    /**
     * 根据角色id获取拥有的权限id列表
     */
    @Override
    public List<Long> getCheckedIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<RolePermission> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissionList = list(wrapper);
        List<Long> checkedIds = rolePermissionList.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return checkedIds;
    }
}
