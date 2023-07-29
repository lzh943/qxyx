package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.sys.RegionWare;
import com.hello.result.Result;

public interface RegionWareService extends IService<RegionWare> {
    /**
     * 分页查询开通区域列表
     * @param page
     * @param limit
     * @param keyword
     * @return
     */
    Result pageRegionWare(Long page, Long limit, String keyword);

    /**
     * 添加开通区域
     * @param regionWare
     */
    void saveRegionWare(RegionWare regionWare);

    /**
     * 管理开通
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);
}
