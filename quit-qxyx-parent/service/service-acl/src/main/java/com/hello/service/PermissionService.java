package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.acl.Permission;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {
    /**
     * 查询全部菜单
     * @return
     */
    List<Permission> queryAllMenu();

    /**
     * 递归删除菜单
     * @param id
     */
    void removeChildById(Long id);

    /**
     * 根据角色获取权限数据
     * @param roleId
     * @return
     */
    List<Permission> findPermissionByroleId(Long roleId);

    /**
     * 根据角色分配权限
     * @param roleId
     * @param permissionId
     */
    void saveRolePermissionRealtionShip(Long roleId, Long[] permissionId);
}
