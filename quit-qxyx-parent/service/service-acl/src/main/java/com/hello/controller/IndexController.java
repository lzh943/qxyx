package com.hello.controller;

import com.hello.exception.SysException;
import com.hello.model.acl.Admin;
import com.hello.result.Result;
import com.hello.service.AdminService;
import com.hello.utils.MD5;
import com.hello.vo.acl.AdminLoginVo;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/acl/index")
@Api(tags = "后台登录管理")
public class IndexController {
    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody AdminLoginVo adminLoginVo){
        Admin admin=adminService.getByUsername(adminLoginVo.getUsername());
        if(admin==null){
            throw new SysException("用户不存在",500);
        }
        if(!MD5.encrypt(adminLoginVo.getPassword()).equals(admin.getPassword())) {
            throw new SysException("密码错误",500);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);
    }

    @GetMapping("/info")
    public Result info(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","admin");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        return Result.ok(map);
    }
    @PostMapping("/logout")
    public Result logout(){
        return Result.ok();
    }
}
