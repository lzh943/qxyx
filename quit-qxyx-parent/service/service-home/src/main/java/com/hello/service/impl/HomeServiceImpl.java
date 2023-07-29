package com.hello.service.impl;

import com.baomidou.mybatisplus.extension.api.R;
import com.hello.model.product.Category;
import com.hello.model.product.SkuInfo;
import com.hello.model.search.SkuEs;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.search.SkuFeignClient;
import com.hello.service.HomeService;
import com.hello.user.UserFeignClient;
import com.hello.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class HomeServiceImpl implements HomeService {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private SkuFeignClient skuFeignClient;
    /**
     * 获取首页数据
     */
    @Override
    public Result homeData(Long userId) {
        HashMap<String,Object> map=new HashMap();
        //获取地址信息
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
            map.put("leaderAddressVo", leaderAddressVo);
        }, threadPoolExecutor);
        //获取分类信息和新人专享商品
        CompletableFuture<Void> productFuture = CompletableFuture.runAsync(() -> {
            List<Category> categoryList = productFeignClient.findAllCategory();
            map.put("categoryList", categoryList);
            List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
            map.put("newPersonSkuInfoList", newPersonSkuInfoList);
        }, threadPoolExecutor);
        //获取热门商品
        CompletableFuture<Void> searchFuture = CompletableFuture.runAsync(() -> {
            List<SkuEs> hotSkuList = skuFeignClient.findHotSkuList();
            map.put("hotSkuList", hotSkuList);
        }, threadPoolExecutor);
        CompletableFuture.allOf(userFuture,productFuture,searchFuture).join();
        return Result.ok(map);
    }
}
