package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuPoster;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    @Override
    public Map getItemBySkuId(Long skuId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        List<SpuPoster> spuPosterList= productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        List<SpuSaleAttr> spuSaleAttrList= productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        HashMap<Object, Object> map = new HashMap<>();
        map.put("skuInfo",skuInfo);
        map.put("spuPosterList",spuPosterList);
        map.put("categoryView",categoryView);
        map.put("price",skuPrice);
        map.put("spuSaleAttrList",spuSaleAttrList);
        return map;
    }
}
