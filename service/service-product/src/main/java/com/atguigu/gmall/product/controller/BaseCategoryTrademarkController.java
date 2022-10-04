package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/4
 * description:
 */
@RestController
@RequestMapping("/admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {
    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;
//    /admin/product/baseCategoryTrademark/findTrademarkList/{category3Id}
//    根据category3Id获取品牌列表
    @GetMapping("findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> baseTrademarkList=baseCategoryTrademarkService.getTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }
//    GET/admin/product/baseCategoryTrademark/findCurrentTrademarkList/{category3Id}
//    获取可选品牌列表
    @GetMapping("findCurrentTrademarkList/{category3Id}")
    public Result findCurrentTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> CurrentTrademarkList=baseCategoryTrademarkService.getCurrentTrademarkList(category3Id);
        return Result.ok(CurrentTrademarkList);
    }
//    POST/admin/product/baseCategoryTrademark/save
//    保存分类关联
    @PostMapping("save")
    public Result saveCategoryTrademark(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.saveCategoryTrademark(categoryTrademarkVo);
        return Result.ok();
    }
//DELETE/admin/product/baseCategoryTrademark/remove/{category3Id}/{trademarkId}
//删除分类
    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result removeCategoryTrademark(@PathVariable Long category3Id,
                                          @PathVariable Long trademarkId){
        baseCategoryTrademarkService.removeCategoryTrademark(category3Id,trademarkId);
        return Result.ok();
    }
}
