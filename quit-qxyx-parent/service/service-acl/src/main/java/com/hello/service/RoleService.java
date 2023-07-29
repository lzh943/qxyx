package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.acl.Role;
import com.hello.result.Result;
import com.hello.vo.acl.RoleQueryVo;

import java.util.Map;

public interface RoleService extends IService<Role> {
    /**
     * 分页查询角色信息
     * @param current
     * @param limit
     * @param roleQueryVo
     * @return
     */
    Result pageRole(Long current, Long limit, RoleQueryVo roleQueryVo);

    /**
     * 根据用户id查询角色信息
     * @param adminId
     * @return
     */
    Map<String, Object> findRoleByUserId(Long adminId);

    /**
     * 根据用户id分配角色
     * @param adminId
     * @param roleId
     */
    void saveUserRoleRealtionShip(Long adminId, Long[] roleId);
}
