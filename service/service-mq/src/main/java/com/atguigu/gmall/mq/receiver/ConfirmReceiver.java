package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * title:
 * author: bai
 * date: 2022/10/21
 * description:
 */
@Component
public class ConfirmReceiver {

    @Autowired
    private RedisTemplate redisTemplate;

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

    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMsg2(String msg,Message message,Channel channel){
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        System.out.println("接收时间"+sdf.format(new Date()));
        System.out.println("接收消息"+msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
//监听延迟插件

    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getMsg3(String msg,Message message,Channel channel){
//        设置一个key，确保不重复
        String key=msg+":key";

        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "0", 30, TimeUnit.SECONDS);

        if (!result){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");


            System.out.println("接收时间"+sdf.format(new Date()));
            System.out.println("接收消息"+msg);
        } catch (Exception e) {
            e.printStackTrace();
            redisTemplate.delete(key);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}
