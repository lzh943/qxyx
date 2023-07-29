package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.Attr;
import com.hello.result.Result;

public interface AttrService extends IService<Attr> {
    /**
     *根据分组id获取属性列表
     * @param attrGroupId
     * @return
     */
    Result findListByAttrGroupId(Long attrGroupId);
}
