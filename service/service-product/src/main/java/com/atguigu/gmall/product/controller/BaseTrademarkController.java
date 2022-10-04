package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;
//    /admin/product/baseTrademark/{page}/{limit}
    @GetMapping("{page}/{limit}")
    public Result getBaseTrademarkList(@PathVariable Long page,
                                       @PathVariable Long limit){
        Page<BaseTrademark> baseTrademarkPage=new Page<>(page,limit);
        IPage iPage= baseTrademarkService.getBaseTrademarkList(baseTrademarkPage);
        return Result.ok(iPage);
    }

//    /admin/product/baseTrademark/save
    @PostMapping("save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }
//    /admin/product/baseTrademark/update
    @PutMapping("update")
    public Result updateBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

//    /admin/product/baseTrademark/remove/{id}
    @DeleteMapping("remove/{id}")
    public Result removeBaseTrademark(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
//    /admin/product/baseTrademark/getTrademarkList
    @GetMapping("getTrademarkList")
    public Result getBaseTrademarkList(){
        List<BaseTrademark> list = baseTrademarkService.list();
        return Result.ok(list);
    }
///admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTrademarkById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }
}
