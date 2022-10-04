package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/4
 * description:
 */
@RestController
@RequestMapping("/admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;
//    /admin/product/{page}/{limit}   getSpuInfoPage
//    根据三级分类，获取spu
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);
        IPage iPage=manageService.getSpuInfoPage(spuInfoPage,spuInfo);
        return Result.ok(iPage);
    }
///admin/product/baseSaleAttrList
//    查询所有平台销售属性
    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList=manageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }
}
