package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMsg")
    public Result sendMsg(){
        rabbitService.sendMsg("exchange.confirm","routing.confirm","死鬼，才回来");
        return Result.ok();
    }

    @GetMapping("sendDeadLetter")
    public Result sendDeadLetter(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"ok");
        System.out.println(sdf.format(new Date())+"Delay sent.");
        return Result.ok();
    }
    @GetMapping("sendDelayMsg")
    public Result sendDelayMsg2(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date())+"Delay sent.");
       rabbitService.sendDelayMsg(DelayedMqConfig.exchange_delay,DelayedMqConfig.routing_delay,"delayed",10);

        return Result.ok();
    }
}
