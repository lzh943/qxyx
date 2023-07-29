package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.PermissionMapper;
import com.hello.model.acl.AdminRole;
import com.hello.model.acl.Permission;
import com.hello.model.acl.RolePermission;
import com.hello.service.PermissionService;
import com.hello.service.RolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Resource
    private RolePermissionService rolePermissionService;
    /**
     * 查询全部菜单
     */
    @Override
    public List<Permission> queryAllMenu() {
        List<Permission> permissionList = buildPermission(list(),0L);
        return permissionList;
    }

    private List<Permission> buildPermission(List<Permission> permissions,Long parentId) {
        List<Permission> trees = permissions.stream()
                .filter(p -> p.getPid().equals(parentId))
                .map(p -> {
                    p.setLevel(1);
                    return p.setChildren(findChildren(p, permissions));
                })
                .collect(Collectors.toList());
        return trees;
    }

    private List<Permission> findChildren(Permission permission, List<Permission> permissions) {
        List<Permission> permissionList = permissions.stream()
                .filter(p -> p.getPid().equals(permission.getId()))
                .map(p -> {
                    return p.setChildren(findChildren(p, permissions));
                })
                .collect(Collectors.toList());
        return permissionList;
    }

    /**
     * 递归删除菜单
     */
    @Override
    public void removeChildById(Long id) {
        List<Long> idList = new ArrayList<>();
        idList.add(id);
        getAllPermissionById(id,idList);
        removeByIds(idList);
    }

    /**
     * 根据角色获取权限数据
     * @return
     */
    @Override
    public List<Permission> findPermissionByroleId(Long roleId) {
        List<Permission> permissions = list();
        List<Long> checkedIds =rolePermissionService.getCheckedIdsByRoleId(roleId);
        permissions.stream()
                .forEach(permission -> {
                    if(checkedIds.contains(permission.getId())){
                        permission.setSelect(true);
                    }else {
                        permission.setSelect(false);
                    }
                });
        List<Permission> permissionList = buildPermission(permissions, 0L);
        return permissionList;
    }

    /**
     * 根据角色分配权限
     * @param roleId
     * @param permissionId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissionRealtionShip(Long roleId, Long[] permissionIds) {
        LambdaQueryWrapper<RolePermission> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionService.remove(wrapper);
        List<RolePermission> rolePermissionsList = new ArrayList<>();
        for(Long permissionId : permissionIds) {
            if(StringUtils.isEmpty(permissionId)) continue;
            RolePermission rolePermission=new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionsList.add(rolePermission);
        }
        rolePermissionService.saveBatch(rolePermissionsList);
    }

    private void getAllPermissionById(Long id,List<Long> idList) {
        LambdaQueryWrapper<Permission> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid, id);
        List<Permission> permissions = list(wrapper);
        permissions.stream().forEach(item->{
                idList.add(item.getId());
                getAllPermissionById(item.getId(), idList);
        });
    }

}
