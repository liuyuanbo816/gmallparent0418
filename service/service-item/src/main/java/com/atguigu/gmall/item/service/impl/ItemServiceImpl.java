package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map getItemBySkuId(Long skuId) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)){
            return new HashMap<>();
        }
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        List<SpuPoster> spuPosterList= productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        List<SpuSaleAttr> spuSaleAttrList= productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.selectBaseAttrInfoListBySkuId(skuId);
        HashMap<Object, Object> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(baseAttrInfoList)){
            List<Object> skuAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                Map attrMap  = new HashMap<>();
                attrMap .put("attrName", baseAttrInfo.getAttrName());
                attrMap .put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());
            map.put("skuAttrList",skuAttrList);
        }
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        String valueJson= JSON.toJSONString(skuValueIdsMap);

        map.put("skuInfo",skuInfo);
        map.put("spuPosterList",spuPosterList);
        map.put("categoryView",categoryView);
        map.put("price",skuPrice);
        map.put("spuSaleAttrList",spuSaleAttrList);
        map.put("valuesSkuJson",valueJson);

        return map;
    }
}
