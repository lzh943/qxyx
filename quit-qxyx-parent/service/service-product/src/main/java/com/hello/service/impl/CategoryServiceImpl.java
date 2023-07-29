package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.CategoryMapper;
import com.hello.model.product.Category;
import com.hello.result.Result;
import com.hello.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 分页查询商品分类信息
     */
    @Override
    public Result pageCategory(Long page, Long limit, String name) {
        Page<Category> pageList=new Page(page,limit);
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like(Category::getName, name);
        }
        Page<Category> categoryPage = baseMapper.selectPage(pageList, wrapper);
        return Result.ok(categoryPage);
    }

    /**
     * 获取排序之后的商品分类列表
     * @return
     */
    @Override
    public Result findAllList() {
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return Result.ok(list(wrapper));
    }

    /**
     * 根据id列表获取分类信息
     *
     * @param categoryIdList
     * @return
     */
    @Override
    public List<Category> findCategoryList(List<Long> categoryIdList) {
        List<Category> categoryList = baseMapper.selectBatchIds(categoryIdList);
        return categoryList;
    }
}
