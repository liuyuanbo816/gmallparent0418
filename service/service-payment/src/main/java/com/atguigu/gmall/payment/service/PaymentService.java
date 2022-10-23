package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * title:
 * author: bai
 * date: 2022/10/23
 * description:
 */
public interface PaymentService {

    void savePaymentInfo(OrderInfo orderInfo,String paymentType);
}
