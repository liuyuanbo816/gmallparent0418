package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;

import java.util.Map;

/**
 * title:
 * author: bai
 * date: 2022/10/19
 * description:
 */
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }
}
