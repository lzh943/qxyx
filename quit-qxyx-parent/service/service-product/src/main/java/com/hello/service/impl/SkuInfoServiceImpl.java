package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.constant.MqConst;
import com.hello.constant.RedisConst;
import com.hello.exception.SysException;
import com.hello.mapper.SkuInfoMapper;
import com.hello.model.product.SkuAttrValue;
import com.hello.model.product.SkuImage;
import com.hello.model.product.SkuInfo;
import com.hello.model.product.SkuPoster;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.*;
import com.hello.utils.BeanCopyUtils;
import com.hello.vo.product.SkuInfoQueryVo;
import com.hello.vo.product.SkuInfoVo;
import com.hello.vo.product.SkuStockLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Resource
    private SkuPosterService skuPosterService;
    @Resource
    private SkuImageService skuImageService;
    @Resource
    private SkuAttrValueService skuAttrValueService;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    /**
     * 分页查询Sku信息
     */
    @Override
    public Result pageSkuInfo(Long page, Long limit, SkuInfoQueryVo skuInfoQueryVo) {
        Page<SkuInfo> pageList=new Page<>(page,limit);
        String skuType = skuInfoQueryVo.getSkuType();
        String keyword = skuInfoQueryVo.getKeyword();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        LambdaQueryWrapper<SkuInfo> wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName,keyword);
        }
        if(!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType,skuType);
        }
        if(!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId,categoryId);
        }
        Page<SkuInfo> skuInfoPage = baseMapper.selectPage(pageList, wrapper);
        return Result.ok(skuInfoPage);
    }

    /**
     * 添加Sku信息
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result saveSkuInfo(SkuInfoVo skuInfoVo) {
        //添加基本信息
        SkuInfo skuInfo = BeanCopyUtils.copyBean(skuInfoVo, SkuInfo.class);
        save(skuInfo);
        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            int sort = 1;
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
                sort++;
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(skuInfo.getId());
                skuImages.setSort(sort);
                sort++;
            }
            skuImageService.saveBatch(skuImagesList);
        }
        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
        return Result.ok();
    }

    /**
     * 根据id查询商品信息
     */
    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        List<SkuImage> skuImageList = skuImageService.findBySkuId(skuId);
        List<SkuPoster> skuPosterList = skuPosterService.findBySkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.findBySkuId(skuId);
        SkuInfoVo skuInfoVo = BeanCopyUtils.copyBean(skuInfo, SkuInfoVo.class);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return skuInfoVo;
    }

    /**
     * 修改商品信息
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        Long id=skuInfoVo.getId();
        SkuInfo skuInfo = BeanCopyUtils.copyBean(skuInfoVo, SkuInfo.class);
        //更新sku信息
        updateById(skuInfo);
        //保存sku详情
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId, id));
        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            int sort = 1;
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(id);
                sort++;
            }
            skuPosterService.saveBatch(skuPosterList);
        }
        //删除sku图片
        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, id));
        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(id);
                skuImages.setSort(sort);
                sort++;
            }
            skuImageService.saveBatch(skuImagesList);
        }
        //删除sku平台属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id));
        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(id);
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    /**
     * 删除商品信息
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void removeSkuInfo(Long id) {
        removeById(id);
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id));
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId, id));
        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, id));
        rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,
                MqConst.ROUTING_GOODS_LOWER,id);
    }

    /**
     * 批量删除商品信息
     */
    @Override
    public void removeSkuInfos(List<Long> idList) {
        removeByIds(idList);
        for (Long id:idList) {
            skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id));
            skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId, id));
            skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, id));
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,
                    MqConst.ROUTING_GOODS_LOWER,id);
        }
    }

    /**
     *商品审核
     */
    @Override
    public void check(Long skuId, Integer status) {
        // 更改发布状态
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setCheckStatus(status);
        updateById(skuInfo);
    }

    /**
     * 商品上架或下架
     */
    @Override
    public void publish(Long skuId, Integer status) {
        // 更改发布状态
        if(status == 1) {
            SkuInfo skuInfo = baseMapper.selectById(skuId);
            skuInfo.setPublishStatus(1);
            updateById(skuInfo);
            //异步发送消息,商品上架
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,
                    MqConst.ROUTING_GOODS_UPPER,skuId);
        } else {
            SkuInfo skuInfoUp = new SkuInfo();
            skuInfoUp.setId(skuId);
            skuInfoUp.setPublishStatus(0);
            updateById(skuInfoUp);
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,
                    MqConst.ROUTING_GOODS_LOWER,skuId);//商品下架
        }
    }

    /**
     * 是否为新人专享
     */
    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setIsNewPerson(status);
        updateById(skuInfo);
    }

    /**
     * 根据skuId列表查询sku数据
     */
    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIds) {
        List<SkuInfo> skuInfoList = listByIds(skuIds);
        return skuInfoList;
    }

    /**
     * 获取新人专享商品信息
     */
    @Override
    public List<SkuInfo> findNewPersonList() {
        LambdaQueryWrapper<SkuInfo> wrapper=new LambdaQueryWrapper<>();
        Page<SkuInfo> page=new Page<>(1,3);
        wrapper.eq(SkuInfo::getIsNewPerson, 1);
        wrapper.eq(SkuInfo::getPublishStatus, 1);
        wrapper.orderByDesc(SkuInfo::getStock);//根据库存做排序
        Page<SkuInfo> skuInfoPage = baseMapper.selectPage(page, wrapper);
        return skuInfoPage.getRecords();
    }

    /**
     * 验证和锁定库存
     */
    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo) {
        if(CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new SysException(ResultCodeEnum.DATA_ERROR);
        }
        skuStockLockVoList.stream().forEach(skuStockLockVo -> {
            checkLock(skuStockLockVo);
        });
        boolean flag = skuStockLockVoList.stream()
                .anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock());
        if(flag){
            skuStockLockVoList.stream()
                    .filter(skuStockLockVo -> skuStockLockVo.getIsLock())
                    .forEach(skuStockLockVo -> {
                        baseMapper.unlock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
                    });
            return false;
        }
        redisTemplate.opsForValue().set(RedisConst.SROCK_INFO+orderNo,skuStockLockVoList );//
        return true;
    }

    /**
     * 订单支付成功,扣减库存
     */
    @Override
    public void minusStock(String orderNo) {
        // 获取锁定库存的缓存信息
        List<SkuStockLockVo> skuStockLockVoList = (List<SkuStockLockVo>)this.redisTemplate.opsForValue().get(RedisConst.SROCK_INFO + orderNo);
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            return ;
        }
        // 减库存
        skuStockLockVoList.forEach(skuStockLockVo -> {
            baseMapper.minusStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
        });
        // 解锁库存之后，删除锁定库存的缓存。以防止重复解锁库存
        this.redisTemplate.delete(RedisConst.SROCK_INFO + orderNo);
    }

    //验证锁定库存
    private void checkLock(SkuStockLockVo skuStockLockVo) {
        RLock fairLock = redissonClient.getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());//公平锁
        fairLock.lock();
        try {
           SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());//验证库存
           if(skuInfo==null){
               skuStockLockVo.setIsLock(false);
           }
           Integer rows=baseMapper.lockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());//锁定库存
           if(rows==1){
               skuStockLockVo.setIsLock(true);
           }
        }finally {
            fairLock.unlock();
        }

    }

}
