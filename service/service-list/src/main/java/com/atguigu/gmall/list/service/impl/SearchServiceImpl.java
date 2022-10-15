package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * title:
 * author: bai
 * date: 2022/10/15
 * description:
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;

//点击增加热度
    @Override
    public void incrHotScore(Long skuId) {
        String hotKey="hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (score%10==0){
            Optional<Goods> optionalGoods= goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(score.longValue());
            goodsRepository.save(goods);
        }
    }
//商品上架，信息放到es中
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        goods.setId(skuId);
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        goods.setPrice(skuPrice.doubleValue());
        goods.setCreateTime(new Date());
        BaseTrademark trademark = productFeignClient.getTrademarkById(skuInfo.getTmId());
        goods.setTmId(trademark.getId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.selectBaseAttrInfoListBySkuId(skuId);
        List<SearchAttr> searchAttrList= baseAttrInfoList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);

        goodsRepository.save(goods);
    }
//商品下架
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }
}
