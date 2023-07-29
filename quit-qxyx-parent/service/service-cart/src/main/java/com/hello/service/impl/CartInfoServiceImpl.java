package com.hello.service.impl;

import com.hello.constant.RedisConst;
import com.hello.enums.SkuType;
import com.hello.exception.SysException;
import com.hello.model.order.CartInfo;
import com.hello.model.product.SkuInfo;
import com.hello.product.ProductFeignClient;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.CartInfoService;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ProductFeignClient productFeignClient;

    /**
     * 添加商品到购物车
     */
    @Override
    public Result addToCart(Long userId, Long skuId, Integer skuNum) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = null;
        //购物车中商品已存在
        if (hashOperations.hasKey(skuId.toString())) {
            cartInfo = hashOperations.get(skuId.toString());
            int currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if (currentSkuNum < 1) {
                return Result.ok();
            }
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //大于限购个数，不能更新个数
            if (currentSkuNum >= cartInfo.getPerLimit()) {
                throw new SysException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            cartInfo.setIsChecked(1);//是否被选中(默认选中)
            cartInfo.setUpdateTime(new Date());
            //购物车中商品不存在
        } else {
            skuNum = 1;
            cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if (null == skuInfo) {
                throw new SysException(ResultCodeEnum.DATA_ERROR);
            }
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        hashOperations.put(skuId.toString(), cartInfo);
        this.setCartKeyExpire(cartKey);
        return Result.ok();
    }
    //定义购物车key
    private String getCartKey(Long userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
    //  设置key的过期时间！
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 删除商品
     */
    @Override
    public Result deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate
                .boundHashOps(this.getCartKey(userId));
        //  判断购物车中是否有该商品！
        if (boundHashOps.hasKey(skuId.toString())){
            boundHashOps.delete(skuId.toString());
        }
        return Result.ok();
    }

    /**
     * 批量删除商品
     */
    @Override
    public Result batchDeleteCart(List<Long> skuIdList, Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
        return Result.ok();
    }

    /**
     * 清空购物车
     */
    @Override
    public Result deleteAllCart(Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        hashOperations.values().forEach(cartInfo -> {
            hashOperations.delete(cartInfo.getSkuId().toString());
        });
        return Result.ok();
    }

    /**
     * 查询购物车列表
     */
    @Override
    public Result getCartListByUserId(Long userId) {
        if(userId==null){
            return Result.ok(new ArrayList<CartInfo>());
        }
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = hashOperations.values();
        if(!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            });
        }
        return Result.ok(cartInfoList);
    }
    public List<CartInfo> getCartList(Long userId){
        if(userId==null){
            return new ArrayList<CartInfo>();
        }
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = hashOperations.values();
        if(!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    /**
     * 根据skuId更新选中信息
     */
    @Override
    public Result checkCart(Long userId, Long skuId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = hashOperations.get(skuId.toString());
        if(cartInfo!=null){
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(skuId.toString(),cartInfo);
            setCartKeyExpire(cartKey);
        }
        return Result.ok();
    }

    /**
     * 对购物项全部选中
     */
    @Override
    public Result checkAllCart(Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String ,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = hashOperations.values();
        if(!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.stream()
                    .forEach(cartInfo -> {
                        cartInfo.setIsChecked(isChecked);
                        String skuId = cartInfo.getSkuId().toString();
                        hashOperations.put(skuId,cartInfo);
                    });
        }
        setCartKeyExpire(cartKey);
        return Result.ok();
    }

    /**
     * 批量选择购物项
     */
    @Override
    public Result batchCHeckCart(Long userId, List<Long> skuIdList, Integer isChecked) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.stream().forEach(skuId->{
            CartInfo cartInfo = hashOperations.get(skuId.toString());
            if(cartInfo!=null){
                cartInfo.setIsChecked(isChecked);
                hashOperations.put(skuId.toString(), cartInfo);
            }
        });
        setCartKeyExpire(cartKey);
        return Result.ok();
    }
    //获取当前用户选中的购物项
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = hashOperations.values();
        List<CartInfo> cartInfos = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked().intValue() == 1)
                .collect(Collectors.toList());
        return cartInfos;
    }

    /**
     * 删除选中的购物项
     * @param userId
     */
    @Override
    public void deleteCheckedSku(Long userId) {
        List<CartInfo> cartCheckedList = getCartCheckedList(userId);
        List<Long> skuIds = cartCheckedList.stream()
                .map(cartInfo -> cartInfo.getSkuId())
                .collect(Collectors.toList());
        batchDeleteCart(skuIds, userId);
    }
}
