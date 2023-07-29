package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.exception.SysException;
import com.hello.mapper.RegionWareMapper;
import com.hello.model.sys.RegionWare;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.RegionWareService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RegionWareServiceImpl extends ServiceImpl<RegionWareMapper, RegionWare>
        implements RegionWareService {
    /**
     * 分页查询开通区域列表
     */
    @Override
    public Result pageRegionWare(Long page, Long limit, String keyword) {
        Page<RegionWare> pageParam=new Page<>(page,limit);
        LambdaQueryWrapper<RegionWare> queryWrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like(RegionWare::getRegionName, keyword)
                    .or().like(RegionWare::getWareName, keyword);
        }
        Page<RegionWare> regionWarePage = baseMapper.selectPage(pageParam, queryWrapper);
        return Result.ok(regionWarePage);
    }

    /**
     * 添加开通区域
     *
     * @param regionWare
     */
    @Override
    public void saveRegionWare(RegionWare regionWare) {
        LambdaQueryWrapper<RegionWare> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(RegionWare::getRegionId, regionWare.getRegionId());
        Integer count = baseMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new SysException(ResultCodeEnum.REGION_OPEN);
        }
        baseMapper.insert(regionWare);
    }

    /**
     * 取消开通区域
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        RegionWare regionWare = baseMapper.selectById(id);
        regionWare.setStatus(status);
        baseMapper.updateById(regionWare);
    }
}
