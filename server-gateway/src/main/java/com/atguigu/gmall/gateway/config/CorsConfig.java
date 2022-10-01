package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

/**
 * title:
 * author: bai
 * date: 2022/10/1
 * description:
 */
@Configuration
public class CorsConfig {
    @Bean
    public WebFilter webFilter(){
        //CorsConfiguration创建对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //设置规则
        corsConfiguration.addAllowedOrigin("*"); //设置允许域名
        corsConfiguration.addAllowedMethod("*");//设置允许请求
        corsConfiguration.addAllowedHeader("*");//设置请求头
        corsConfiguration.setAllowCredentials(true);//允许携带cookie
//创建UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
