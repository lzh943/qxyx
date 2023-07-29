package com.hello.service;

import com.hello.result.Result;

public interface ItemService {
    /**
     * 查询商品详细信息
     * @param id
     * @param userId
     * @return
     */
    Result item(Long skuId, Long userId);
}
