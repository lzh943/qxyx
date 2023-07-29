package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.activity.ActivitySku;

import java.util.List;

public interface ActivitySkuService extends IService<ActivitySku> {
    /**
     * 根据商品id列表获取活动id
     * @param skuIdList
     * @return
     */
    List<ActivitySku> selectCartActivity(List<Long> skuIdList);
}
