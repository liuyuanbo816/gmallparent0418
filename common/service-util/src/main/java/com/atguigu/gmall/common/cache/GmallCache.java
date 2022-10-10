package com.atguigu.gmall.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * title:
 * author: bai
 * date: 2022/10/10
 * description:
 */
@Target({ElementType.METHOD})//在哪里使用
@Retention(RetentionPolicy.RUNTIME)//注解的声明周期
public @interface GmallCache {
//    定义一个前缀
    String prefix() default "cache";
}
