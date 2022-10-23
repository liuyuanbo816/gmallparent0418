package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * title:
 * author: bai
 * date: 2022/10/23
 * description:
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
//存储交易记录
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        PaymentInfo paymentInfoQuery = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("order_id", orderInfo.getId()).eq("payment_type", paymentType));
//        已支付
        if (paymentInfoQuery!=null){
            return;
        }
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
//        paymentInfo.setTradeNo(); 支付宝交易编号
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfoMapper.insert(paymentInfo);
    }
}
