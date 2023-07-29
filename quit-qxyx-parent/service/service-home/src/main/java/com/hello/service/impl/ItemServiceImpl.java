package com.hello.service.impl;

import com.hello.activity.ActivityFeignClient;
import com.hello.model.product.SkuInfo;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.search.SkuFeignClient;
import com.hello.service.ItemService;
import com.hello.vo.product.SkuInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private ActivityFeignClient activityFeignClient;
    @Resource
    private SkuFeignClient skuFeignClient;
    /**
     * 查询商品详细信息
     */
    @Override
    public Result item(Long skuId, Long userId) {
        Map<String,Object> map=new HashMap();
        //获取商品信息
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoVo skuInfoVo = productFeignClient.findSkuInfoVoBySkuId(skuId);
            map.put("skuInfoVo", skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);
        //获取商品的优惠卷信息
        CompletableFuture<Void> activityCompletableFuture = CompletableFuture.runAsync(() -> {
            Map<String, Object> activityMap = activityFeignClient.findActivityAndCoupon(skuId, userId);
            map.putAll(activityMap);
        },threadPoolExecutor);
        //更新商品热度
        CompletableFuture<Void> hotCompletableFuture = CompletableFuture.runAsync(() -> {
            skuFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);
        CompletableFuture.allOf(skuInfoVoCompletableFuture,activityCompletableFuture
                ,hotCompletableFuture).join();
        return Result.ok(map);
    }
}
