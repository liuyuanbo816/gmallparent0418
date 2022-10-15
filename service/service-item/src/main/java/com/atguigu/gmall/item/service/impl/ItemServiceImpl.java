package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * title:
 * author: bai
 * date: 2022/10/6
 * description:
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map getItemBySkuId(Long skuId) {
        HashMap<Object, Object> map = new HashMap<>();
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        if (!bloomFilter.contains(skuId)){
//            return new HashMap<>();
//        }
//获取基本信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
//        获取海报信息
        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            map.put("spuPosterList", spuPosterList);
        }, threadPoolExecutor);
//        分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        }, threadPoolExecutor);
//实时价格
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price",skuPrice);
        }, threadPoolExecutor);
//        销售属性
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPoolExecutor);

//        规格与包装
        CompletableFuture<Void> baseAttrInfoCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<BaseAttrInfo> baseAttrInfoList = productFeignClient.selectBaseAttrInfoListBySkuId(skuId);

            if (!CollectionUtils.isEmpty(baseAttrInfoList)) {
                List<Object> skuAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                    Map attrMap = new HashMap<>();
                    attrMap.put("attrName", baseAttrInfo.getAttrName());
                    attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return attrMap;
                }).collect(Collectors.toList());
                map.put("skuAttrList", skuAttrList);
            }
        }, threadPoolExecutor);

//显示商品销售属性并切换
        CompletableFuture<Void> skuValueCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valueJson = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", valueJson);
        }, threadPoolExecutor);

//        点击增加热度
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);
        CompletableFuture.allOf(skuValueCompletableFuture,
                baseAttrInfoCompletableFuture,
                spuSaleAttrCompletableFuture,
                skuPriceCompletableFuture,
                categoryViewCompletableFuture,
                spuPosterListCompletableFuture,
                skuInfoCompletableFuture
                ).join();
        return map;
    }
}
