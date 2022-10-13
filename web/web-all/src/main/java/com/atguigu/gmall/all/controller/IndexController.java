package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/13
 * description:
 */
@Controller
public class IndexController {
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping({"/","index.html"})
    public String index(Model model){

        List<JSONObject> list = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",list);
        return "index/index";
    }
//    创建一个静态化页面
    @GetMapping("createIndex")
    @ResponseBody
    public Result createIndex(){

        Context context = new Context();
        List<JSONObject> list = productFeignClient.getBaseCategoryList();
        context.setVariable("list",list);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D://index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        templateEngine.process("index/index.html",context,fileWriter);

        return Result.ok();
    }
}
