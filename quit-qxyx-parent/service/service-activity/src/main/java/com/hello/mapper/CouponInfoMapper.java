package com.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.activity.CouponInfo;
import com.hello.model.order.CartInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {
    /**
     * 根据参数查询对应的优惠劵信息
     * @param skuId
     * @param categoryId
     * @param userId
     * @return
     */
    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long skuId,@Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId);

    /**
     * 查询用户拥有的所有优惠劵信息
     * @param userId
     * @return
     */
    List<CouponInfo> selectCartCouponList(Long userId);
}
