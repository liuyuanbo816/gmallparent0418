package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * title:
 * author: bai
 * date: 2022/10/21
 * description:
 */
@Service
public class RabbitService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendMsg(String exchange,String routingKey,Object msg){
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();

        String correlationDataId= UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationDataId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);

        redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData),90, TimeUnit.SECONDS);
        rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),gmallCorrelationData.getMessage(),gmallCorrelationData);

        return true;
    }

    public boolean sendDelayMsg(String exchange, String routingKey, Object msg, int delayTime) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String correlationDataId=UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationDataId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);
        gmallCorrelationData.setDelayTime(delayTime);
        gmallCorrelationData.setDelay(true);
        redisTemplate.opsForValue().set(correlationDataId,JSON.toJSONString(gmallCorrelationData),90,TimeUnit.SECONDS);
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,message -> {
            message.getMessageProperties().setDelay(delayTime*1000);
            return message;
        },gmallCorrelationData);
        return true;
    }
}
