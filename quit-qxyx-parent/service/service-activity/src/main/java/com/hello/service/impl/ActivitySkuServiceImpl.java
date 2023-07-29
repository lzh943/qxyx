package com.hello.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.ActivitySkuMapper;
import com.hello.model.activity.ActivitySku;
import com.hello.service.ActivitySkuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivitySkuServiceImpl extends ServiceImpl<ActivitySkuMapper, ActivitySku>
        implements ActivitySkuService {
    /**
     * 根据商品id列表获取活动id
     */
    @Override
    public List<ActivitySku> selectCartActivity(List<Long> skuIdList) {
        return baseMapper.selectCartActivity(skuIdList);
    }
}
