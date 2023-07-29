package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.acl.Admin;
import com.hello.result.Result;
import com.hello.vo.acl.AdminLoginVo;
import com.hello.vo.acl.AdminQueryVo;

public interface AdminService extends IService<Admin> {
    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    Admin getByUsername(String username);

    /**
     * 分页查找用户
     * @param current
     * @param limit
     * @param adminQueryVo
     * @return
     */
    Result pageAdmin(Long current, Long limit, AdminQueryVo adminQueryVo);
}
