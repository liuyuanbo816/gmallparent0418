package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/5
 * description:
 */
@RestController
@RequestMapping("admin/product")
public class SkuManageController {
    @Autowired
    private ManageService manageService;

    //    接口路径：GET/admin/product/spuImageList/{spuId}
//    通过spuId获取spuimagelist
    @GetMapping("/spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId) {
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //    GET/admin/product/spuSaleAttrList/{spuId}
//  通过spuId获得spuSaleAttrList
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    //    接口路径：POST/admin/product/saveSkuInfo
//    新增sku保存
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //    GET/admin/product/list/{page}/{limit}
//    分页查询sku信息
    @GetMapping("list/{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SkuInfo skuInfo) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage iPage = manageService.getSkuInfoPage(skuInfoPage, skuInfo);
        return Result.ok(iPage);
    }

    //    /admin/product/onSale/{skuId}
//    上架sku
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        manageService.onSale(skuId);
        return Result.ok();
    }

    //    /admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        manageService.cancelSale(skuId);
        return Result.ok();
    }
}
