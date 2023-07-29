package com.hello.controller;

import com.hello.model.acl.Admin;
import com.hello.model.acl.Role;
import com.hello.result.Result;
import com.hello.service.AdminService;
import com.hello.service.RoleService;
import com.hello.utils.MD5;
import com.hello.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/acl/user")
@Api(tags = "用户管理")
public class AdminController {
    @Resource
    private AdminService adminService;
    @Resource
    private RoleService roleService;


    @ApiOperation(value = "获取管理用户分页列表")
    @GetMapping("/{current}/{limit}")
    public Result listAdmin(@PathVariable Long current, @PathVariable Long limit, AdminQueryVo adminQueryVo){
        return adminService.pageAdmin(current,limit,adminQueryVo);
    }
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{adminId}")
    public Result toAssign(@PathVariable Long adminId) {
        Map<String, Object> roleMap = roleService.findRoleByUserId(adminId);
        return Result.ok(roleMap);
    }
    @ApiOperation(value = "根据用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long adminId,@RequestParam Long[] roleId) {
        roleService.saveUserRoleRealtionShip(adminId,roleId);
        return Result.ok();
    }
    @ApiOperation(value = "获取管理用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Admin user = adminService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation(value = "新增管理用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin user) {
        //对密码进行MD5处理
        user.setPassword(MD5.encrypt(user.getPassword()));
        adminService.save(user);
        return Result.ok();
    }

    @ApiOperation(value = "修改管理用户")
    @PutMapping("update")
    public Result updateById(@RequestBody Admin user) {
        adminService.updateById(user);
        return Result.ok();
    }

    @ApiOperation(value = "删除管理用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        adminService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "根据id列表删除管理用户")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        adminService.removeByIds(idList);
        return Result.ok();
    }
}
