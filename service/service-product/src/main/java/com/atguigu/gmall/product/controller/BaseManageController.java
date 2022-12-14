package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/9/29
 * description:
 */
@Api("商品基础属性接口")
@RestController
@RequestMapping("/admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;
//    /admin/product/getCategory1
    //获取一级目录
    @GetMapping("getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> baseCategory1List=manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

//    /admin/product/getCategory2/{category1Id}
//    获取二级目录
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> baseCategory2List=manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

//    /admin/product/getCategory3/{category2Id}
//    获取三级目录
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> baseCategory3List=manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

//    /admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
//    根据目录名称获取系统属性
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList(@PathVariable Long category1Id,
                                  @PathVariable Long category2Id,
                                  @PathVariable Long category3Id){

        List<BaseAttrInfo> baseAttrInfoList=manageService.selectAttrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfoList);
    }
///admin/product/saveAttrInfo
//    通过判定attrid是否存在，进行保存平台属性或删除平台属性
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }
//   "/getAttrValueList/{attrId}"
//    通过attrid查询平台属性值并show
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
//        List<BaseAttrValue> baseAttrValueList=manageService.getAttrValueList(attrId);
        //业务需求进行修改，先查询平台属性，再获得平台属性值
       BaseAttrInfo baseAttrInfo=manageService.getBaseAttrInfo(attrId);
        return Result.ok(baseAttrInfo.getAttrValueList());
    }

//    修改某个数据
    @PutMapping("update")
    public Result updateCategory3ById(@RequestBody BaseCategory3 baseCategory3){
        manageService.updateCategory3ById(baseCategory3);
        return Result.ok();
    }


}
