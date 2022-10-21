package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * title:
 * author: bai
 * date: 2022/10/21
 * description:
 */
@Component
public class ConfirmReceiver {

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    public void getMsg(String msg, Message message, Channel channel){
//消息消费失败如何处理
//        1使用nack应答机制，让其重回队列,可以使用redis控制重试次数
//        2所有消息都确认处理，记录的消息表中，后续进行处理
        try {
            System.out.println("msg = " + msg);
//            int i= 1/0;
            System.out.println(new String(message.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("重回队列");
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
