package com.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.product.SkuInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    /**
     * 解锁库存
     * @param skuId
     * @param skuNum
     */
    void unlock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    /**
     * 锁定库存
     * @param skuId
     * @param skuNum
     * @return
     */
    Integer lockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    /**
     * 订单支付成功后,扣减库存
     * @param skuId
     * @param skuNum
     * @return
     */
    Integer minusStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);
}
