package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * title:
 * author: bai
 * date: 2022/10/20
 * description:
 */
public interface OrderService {
    String getTradeNo(String userId);

    boolean checkTradeNo(String tradeNo, String userId);

    void delTradeNo(String userId);

    boolean checkStock(Long skuId, Integer skuNum);

    Long saveOrderInfo(OrderInfo orderInfo);

    IPage<OrderInfo> getOrderPage(Page<OrderInfo> orderInfoPage, String userId);
}
