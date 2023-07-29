package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.AdminMapper;
import com.hello.model.acl.Admin;
import com.hello.result.Result;
import com.hello.service.AdminService;
import com.hello.vo.acl.AdminQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper,Admin> implements AdminService {
    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    @Override
    public Admin getByUsername(String username) {
        LambdaQueryWrapper<Admin> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username);
        return getOne(queryWrapper);
    }

    /**
     * 分页查找用户
     */
    @Override
    public Result pageAdmin(Long current, Long limit, AdminQueryVo adminQueryVo) {
        Page<Admin> page=new Page<>(current,limit);
        String name = adminQueryVo.getName();
        String username = adminQueryVo.getUsername();
        LambdaQueryWrapper<Admin> queryWrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(username)){
            queryWrapper.eq(Admin::getUsername, username);
        }
        if(!StringUtils.isEmpty(name)){
            queryWrapper.like(Admin::getName, name);
        }
        Page<Admin> adminPage = baseMapper.selectPage(page, queryWrapper);
        return Result.ok(adminPage);
    }
}
