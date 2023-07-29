package com.hello.controller;

import com.hello.auth.AuthContextHolder;
import com.hello.model.user.User;
import com.hello.result.Result;
import com.hello.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user/weixin")
public class WeChatApiController {
    @Resource
    private UserService userService;

    @ApiOperation(value = "用户微信授权登录")
    @GetMapping("/wxLogin/{code}")
    public Result loginWx(@PathVariable String code){
        return userService.loginWx(code);
    }
    @ApiOperation(value = "更新用户昵称与头像")
    @PostMapping("/auth/updateUser")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok();
    }
}
