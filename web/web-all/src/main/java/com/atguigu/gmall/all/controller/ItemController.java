package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * title:
 * author: bai
 * date: 2022/10/8
 * description:
 */
@Controller
public class ItemController {
    @Autowired
    private ItemFeignClient itemFeignClient;

    @GetMapping("{skuId}.html")
    public String skuIdItem(@PathVariable Long skuId, Model model){
        Result<Map> result = itemFeignClient.getItemBySkuId(skuId);
        model.addAllAttributes(result.getData());
        return "item/item";
    }

}
