package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * title:
 * author: bai
 * date: 2022/10/21
 * description:
 */
@Configuration
@Log4j2
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

//    发送消息失败采用重试机制


//    将重试设置装载到rt中
    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setConfirmCallback(this);
    }
//    交换机确认
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
            System.out.println("消息到达交换机");
        }else {
            log.error("消息未到交换机");
            retrySendMsg(correlationData);
        }
    }


//    队列确认,消息未到队列执行下面方法
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体"+new String(message.getBody()));
        System.out.println("应答码:"+replyCode);
        System.out.println("描述"+replyText);
        System.out.println("消息使用的交换器 exchange"+exchange);
        System.out.println("消息使用的路由键 routing"+routingKey);

//        获取correlationDataId
        String correlationDataId=(String)message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");

//        从缓存中获取数据

        String strJson = (String) redisTemplate.opsForValue().get(correlationDataId);
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(strJson, GmallCorrelationData.class);
        retrySendMsg(gmallCorrelationData);
    }

    private void retrySendMsg(CorrelationData correlationData) {
        GmallCorrelationData gmallCorrelationData= (GmallCorrelationData) correlationData;

        int retryCount=gmallCorrelationData.getRetryCount();
        if (retryCount>2){
//            已重试0,1,2
            return;
        }else {
            retryCount++;
            gmallCorrelationData.setRetryCount(retryCount);
            redisTemplate.opsForValue().set(correlationData.getId(),JSON.toJSONString(gmallCorrelationData),90, TimeUnit.SECONDS);
            if (gmallCorrelationData.isDelay()){
                System.out.println("延迟消息");
                rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),gmallCorrelationData.getMessage(),message -> {
                    message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime()*1000);
                    return message;
                },gmallCorrelationData);
            }else {
                rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),gmallCorrelationData.getMessage(),gmallCorrelationData);
            }
        }
    }
}
