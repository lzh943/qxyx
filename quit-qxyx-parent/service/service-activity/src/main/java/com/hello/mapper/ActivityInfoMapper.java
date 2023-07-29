package com.hello.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.activity.ActivityInfo;
import com.hello.model.activity.ActivityRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    //排除之前参加过活动的商品
    List<Long> selectSkuIdListExist(@Param("skuIds") List<Long> skuIds);
    //根据商品id查询对应活动的规则
    List<ActivityRule> findActivityRule(Long skuId);
}
