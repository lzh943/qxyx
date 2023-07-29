package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.Category;
import com.hello.result.Result;

import java.util.List;

public interface CategoryService extends IService<Category> {
    /**
     * 分页查询商品分类信息
     * @param page
     * @param limit
     * @param name
     * @return
     */
    Result pageCategory(Long page, Long limit, String name);

    /**
     * 获取排序之后的商品分类列表
     * @return
     */
    Result findAllList();

    /**
     * 根据id列表获取分类信息
     * @param categoryIdList
     * @return
     */
    List<Category> findCategoryList(List<Long> categoryIdList);
}
