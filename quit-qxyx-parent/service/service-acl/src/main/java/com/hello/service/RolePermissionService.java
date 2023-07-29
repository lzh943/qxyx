package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.acl.RolePermission;

import java.util.List;

public interface RolePermissionService extends IService<RolePermission> {
    /**
     * 根据角色id获取拥有的权限id列表
     * @param roleId
     * @return
     */
    List<Long> getCheckedIdsByRoleId(Long roleId);
}
