package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.product.SkuInfo;
import com.hello.result.Result;
import com.hello.vo.product.SkuInfoQueryVo;
import com.hello.vo.product.SkuInfoVo;
import com.hello.vo.product.SkuStockLockVo;

import java.util.List;

public interface SkuInfoService extends IService<SkuInfo> {
    /**
     * 分页查询Sku信息
     * @param page
     * @param limit
     * @param skuInfoQueryVo
     * @return
     */
    Result pageSkuInfo(Long page, Long limit, SkuInfoQueryVo skuInfoQueryVo);

    /**
     * 添加Sku信息
     * @param skuInfoVo
     * @return
     */
    Result saveSkuInfo(SkuInfoVo skuInfoVo);

    /**
     * 根据id查询商品信息
     * @param id
     * @return
     */
    SkuInfoVo getSkuInfoVo(Long id);

    /**
     * 修改商品信息
     * @param skuInfoVo
     */
    void updateSkuInfo(SkuInfoVo skuInfoVo);

    /**
     * 删除商品信息
     * @param id
     */
    void removeSkuInfo(Long id);

    /**
     * 批量删除商品信息
     * @param idList
     */
    void removeSkuInfos(List<Long> idList);

    /**
     * 商品审核
     * @param skuId
     * @param status
     */
    void check(Long skuId, Integer status);

    /**
     * 商品上架
     * @param skuId
     * @param status
     */
    void publish(Long skuId, Integer status);

    /**
     * 是否为新人专享
     * @param skuId
     * @param status
     */
    void isNewPerson(Long skuId, Integer status);

    /**
     * 根据skuId列表查询sku数据
     * @param skuIds
     * @return
     */
    List<SkuInfo> findSkuInfoList(List<Long> skuIds);

    /**
     * 获取新人专享商品信息
     * @return
     */
    List<SkuInfo> findNewPersonList();

    /**
     * 验证和锁定库存
     * @param skuStockLockVoList
     * @param orderNo
     * @return
     */
    Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo);

    /**
     * 订单支付成功,扣减库存
     * @param orderNo
     */
    void minusStock(String orderNo);
}
