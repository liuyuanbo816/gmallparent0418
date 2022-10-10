package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * title:
 * author: bai
 * date: 2022/10/10
 * description:
 */
@Aspect
@Component
public class GmallCacheImpl {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

//    定义环绕通知
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheJoinPoint(ProceedingJoinPoint point) throws Throwable {
        Object object=null;
//1获取key
//        2key获取缓存
//        true：返回 false：查db
        Object[] args = point.getArgs();//获取方法参数
        System.out.println("args = " + args);

        MethodSignature methodSignature = (MethodSignature) point.getSignature();//获取方法信息
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();
        String key=prefix+ Arrays.asList(args).toString();

//        获取缓存
        try {
            object=this.getRedisData(key,methodSignature);
            if (object==null){//缓存为空
                String locKey=key+":lock";
                RLock lock=redissonClient.getLock(locKey);
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res){
    //                获取锁，查询数据库
                    try {
                        object=point.proceed();//执行注解下的方法体
                        if (object==null){
        //            数据库为空,往redis中存一个空对象
                            Object object1 = new Object();
                            this.redisTemplate.opsForValue().set(key,JSON.toJSONString(object1),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                            return object1;
                        }
    //                将数据库中对象存入redis
                        this.redisTemplate.opsForValue().set(key,JSON.toJSONString(object),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return object;
                    } finally {
                            lock.unlock();
                    }
                }else {//没获得锁，自旋
                    Thread.sleep(200);
                    return gmallCacheJoinPoint(point);
                }
            }else {//在缓存中有数据
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //兜底查询数据库
        return point.proceed();
    }

    private Object getRedisData(String key, MethodSignature methodSignature) {
        String strJson = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(strJson)){
            return JSON.parseObject(strJson,methodSignature.getReturnType());
        }
        return null;
    }

}
