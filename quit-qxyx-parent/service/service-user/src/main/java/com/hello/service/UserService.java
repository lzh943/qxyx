package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.user.User;
import com.hello.result.Result;

public interface UserService extends IService<User> {
    /**
     * 用户微信登录授权
     * @param code
     * @return
     */
    Result loginWx(String code);
}
