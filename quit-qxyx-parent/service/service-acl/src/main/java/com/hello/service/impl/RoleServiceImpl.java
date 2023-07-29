package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.RoleMapper;
import com.hello.model.acl.AdminRole;
import com.hello.model.acl.Role;
import com.hello.result.Result;
import com.hello.service.AdminRoleService;
import com.hello.service.RoleService;
import com.hello.vo.acl.RoleQueryVo;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper,Role> implements RoleService {
    @Resource
    AdminRoleService adminRoleService;
    /**
     * 分页查询角色信息
     */
    @Override
    public Result pageRole(Long current, Long limit, RoleQueryVo roleQueryVo) {
        Page<Role> page=new Page<>(current,limit);
        String roleName = roleQueryVo.getRoleName();
        LambdaQueryWrapper<Role> queryWrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(roleName)){
            queryWrapper.like(Role::getRoleName, roleName);
        }
        Page<Role> rolePage = baseMapper.selectPage(page, queryWrapper);
        return Result.ok(rolePage);
    }
    /**
     * 根据用户id查询角色信息
     * @param adminId
     * @return
     */
    @Override
    public Map<String, Object> findRoleByUserId(Long adminId) {
        //查询所有的角色
        List<Role> allRolesList =list();
        //拥有的角色id
        List<AdminRole> existUserRoleList = adminRoleService
                .list(new QueryWrapper<AdminRole>()
                        .eq("admin_id", adminId).select("role_id"));
        List<Long> existRoleList = existUserRoleList.stream()
                .map(c->c.getRoleId()).collect(Collectors.toList());
        //对角色进行分类
        List<Role> assignRoles = new ArrayList<Role>();
        for (Role role : allRolesList) {
            //已分配
            if(existRoleList.contains(role.getId())) {
                assignRoles.add(role);
            }
        }
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assignRoles", assignRoles);
        roleMap.put("allRolesList", allRolesList);
        return roleMap;
    }

    /**
     * 根据用户id分配角色
     * @param adminId
     * @param roleId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserRoleRealtionShip(Long adminId, Long[] roleIds) {
        //删除用户分配的角色数据
        adminRoleService.remove(new QueryWrapper<AdminRole>().eq("admin_id", adminId));
        //分配新的角色
        List<AdminRole> userRoleList = new ArrayList<>();
        for(Long roleId : roleIds) {
            if(StringUtils.isEmpty(roleId)) continue;
            AdminRole userRole = new AdminRole();
            userRole.setAdminId(adminId);
            userRole.setRoleId(roleId);
            userRoleList.add(userRole);
        }
        adminRoleService.saveBatch(userRoleList);
    }

}
