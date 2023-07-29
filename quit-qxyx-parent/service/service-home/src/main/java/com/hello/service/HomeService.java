package com.hello.service;

import com.hello.result.Result;

public interface HomeService {
    /**
     * 获取首页数据
     * @param userId
     * @return
     */
    Result homeData(Long userId);
}
