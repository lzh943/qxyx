package com.hello.service.impl;

import com.alibaba.fastjson.JSON;
import com.hello.activity.ActivityFeignClient;
import com.hello.auth.AuthContextHolder;
import com.hello.enums.SkuType;
import com.hello.exception.SysException;
import com.hello.model.product.Category;
import com.hello.model.product.SkuInfo;
import com.hello.model.search.SkuEs;
import com.hello.product.ProductFeignClient;
import com.hello.repository.SkuRepository;
import com.hello.result.ResultCodeEnum;
import com.hello.service.SkuService;
import com.hello.vo.search.SkuEsQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SkuServiceImpl implements SkuService {
    @Resource
    private SkuRepository skuRepository;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private ActivityFeignClient activityFeignClient;
    @Override
    public void upperSku(Long skuId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo == null) {
            throw new SysException(ResultCodeEnum.DATA_ERROR);
        }
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());
        SkuEs skuEs = new SkuEs();
        if (category != null) {
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName() + "," + skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if (skuInfo.getSkuType() == SkuType.COMMON.getCode()) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        }else {
            //TODO 秒杀商品
        }
        SkuEs save = skuRepository.save(skuEs);
        log.info("upperSku："+ JSON.toJSONString(save));

    }

    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }
    /*
     查询热门商品
     */
    @Override
    public List<SkuEs> findHotSkuList() {
        Pageable pageable= PageRequest.of(0, 10);
        Page<SkuEs> skuEsPage=skuRepository.findByOrderByHotScoreDesc(pageable);
        List<SkuEs> skuEsList = skuEsPage.getContent();
        return skuEsList;
    }

    @Override
    public Page<SkuEs> getSKuEsListByCategoryId(SkuEsQueryVo skuEsQueryVo, Pageable pageable) {
        Long wareId = skuEsQueryVo.getWareId();
        if(wareId==null){
            wareId=1L;
        }
        skuEsQueryVo.setWareId(wareId);
        Page<SkuEs> skuEsPage=null;
        if(StringUtils.isEmpty(skuEsQueryVo.getKeyword())){
            skuEsPage=skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId()
                    ,skuEsQueryVo.getWareId(),pageable);
        }else {
            skuEsPage=skuRepository.findByKeywordAndWareId(skuEsQueryVo.getKeyword()
                    ,skuEsQueryVo.getWareId(),pageable);
        }
        List<SkuEs> skuEsList = skuEsPage.getContent();
        if(!CollectionUtils.isEmpty(skuEsList)){
            List<Long> skuIdList = skuEsList.stream().map(skuEs -> skuEs.getId())
                    .collect(Collectors.toList());
            //key为skuId,value为参加的活动的规则(同一时期商品只能参加一个活动)
            Map<Long, List<String>> skuIdToRuleListMap=activityFeignClient.findActivity(skuIdList);
            if(null != skuIdToRuleListMap) {
                skuEsList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }
        return skuEsPage;
    }

    @Override
    public void incrHotScore(Long skuId) {
        // 定义key
        String hotKey = "hotScore";
        // 保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (hotScore%10==0){
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            skuRepository.save(skuEs);
        }
    }
}
