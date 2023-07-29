package com.hello.service;

import com.hello.model.order.CartInfo;
import com.hello.result.Result;

import java.util.List;

public interface CartInfoService {
    /**
     * 添加商品到购物车
     * @param userId
     * @param skuId
     * @param skuNum
     * @return
     */
    Result addToCart(Long userId, Long skuId, Integer skuNum);

    /**
     * 删除商品
     * @param skuId
     * @param userId
     * @return
     */
    Result deleteCart(Long skuId, Long userId);

    /**
     * 批量删除商品
     * @param skuIdList
     * @param userId
     * @return
     */
    Result batchDeleteCart(List<Long> skuIdList, Long userId);

    /**
     * 清空购物车
     * @param userId
     * @return
     */
    Result deleteAllCart(Long userId);

    /**
     * 查询购物车列表
     * @param userId
     * @return
     */
    Result getCartListByUserId(Long userId);

    List<CartInfo> getCartList(Long userId);

    /**
     * 根据skuId更新选中信息
     * @param userId
     * @param skuId
     * @param isChecked
     * @return
     */
    Result checkCart(Long userId, Long skuId, Integer isChecked);

    /**
     * 对购物项全部选中
     * @param userId
     * @param isChecked
     * @return
     */
    Result checkAllCart(Long userId, Integer isChecked);

    /**
     * 批量选择购物项
     * @param userId
     * @param skuIdList
     * @param isChecked
     * @return
     */
    Result batchCHeckCart(Long userId, List<Long> skuIdList, Integer isChecked);
    //获取当前用户选中的购物项
    List<CartInfo> getCartCheckedList(Long userId);
    //删除选中的购物项
    void deleteCheckedSku(Long userId);
}
