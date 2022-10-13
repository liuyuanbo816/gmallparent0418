package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * title:
 * author: bai
 * date: 2022/10/6
 * description:
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        return manageService.getSkuInfo(skuId);
    }

//    接口路径：GET/api/product/inner/findSpuPosterBySpuId/{spuId}
//    根据spuid获得海报信息
    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId){
        return manageService.getSpuPosterBySpuId(spuId);
    }
//接口路径：GET/api/product/inner/getCategoryView/{category3Id}
//    根据分类Id获取分类名称
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        return manageService.getCategoryView(category3Id);
    }
//    接口路径：GET/api/product/inner/getSkuPrice/{skuId}
//    根据skuid获取实时价格
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return manageService.getSkuPrice(skuId);
    }
//    接口路径：GET/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}
//    根据sku、spu查询销售属性，并锁定
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }
    //  商品切换数据
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

//        //  获取规格与包装
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(@PathVariable Long skuId){
        return manageService.selectBaseAttrInfoListBySkuId(skuId);
    }

//    将首页分类数据发给web-all
    @GetMapping("getBaseCategoryList")
    public List<JSONObject> getBaseCategoryList(){
        return manageService.getCategoryList();
    }

    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable Long tmId){
        return baseTrademarkService.getById(tmId);
    }

}
