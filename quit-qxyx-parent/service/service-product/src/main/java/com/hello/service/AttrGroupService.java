package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.AttrGroup;
import com.hello.result.Result;

public interface AttrGroupService extends IService<AttrGroup> {
    /**
     * 分页查询平台属性分组
     * @param page
     * @param limit
     * @param name
     * @return
     */
    Result pageAttrGroup(Long page, Long limit, String name);

}

