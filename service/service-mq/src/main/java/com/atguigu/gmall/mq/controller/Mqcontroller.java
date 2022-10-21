package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * title:
 * author: bai
 * date: 2022/10/21
 * description:
 */
@RestController
@RequestMapping("mq")
public class Mqcontroller {

    @Autowired
    private RabbitService rabbitService;

    @GetMapping("sendMsg")
    public Result sendMsg(){
        rabbitService.sendMsg("exchange.confirm","routing.confirm","死鬼，才回来");
        return Result.ok();
    }
}
