package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * title:
 * author: bai
 * date: 2022/10/19
 * description:
 */
@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("trade.html")
    public String trade(Model model){

        Map<String, Object> map = orderFeignClient.trade().getData();
        model.addAllAttributes(map);
//        封装tymtheaf
        return "order/trade";
    }

    @GetMapping("myOrder.html")
    public String myOrder(){
//        异步查询
        return "order/myOrder";
    }
}
