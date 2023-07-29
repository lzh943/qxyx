package com.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hello.model.activity.ActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivitySkuMapper extends BaseMapper<ActivitySku> {

    List<ActivitySku> selectCartActivity(List<Long> skuIdList);
}
