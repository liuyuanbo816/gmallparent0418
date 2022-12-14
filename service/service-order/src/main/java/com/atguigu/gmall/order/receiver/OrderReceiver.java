package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * title:
 * author: bai
 * date: 2022/10/23
 * description:
 */
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel){
        try {
            if (orderId!=null){
                OrderInfo orderInfo = orderService.getById(orderId);
                if (orderInfo!=null&&"UNPAID".equals(orderInfo.getOrderStatus())&&"UNPAID".equals(orderInfo.getProcessStatus())){
                         orderService.execExpiredOrder(orderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
