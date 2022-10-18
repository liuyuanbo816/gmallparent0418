package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/17
 * description:
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${authUrls.url}")
    private String authUrls;
//    			网关鉴权，登录与网关整合
//				1.	限制用户通过浏览器直接访问内部数据
//						http://localhost/api/product/inner/getSkuInfo/27
//
//				2.	限制用户访问带有 /*/auth/** 这样的路径
//						如果带有，则必须要在登录的情况下才能访问！
//
//						http://localhost/api/order/auth/trade 如果不登录，提示信息，没有登录。
//
//				3.	限制用户访问web应用，带有{myOrder.thml,trade.html} ，跳转到登录页面！
//
//				4.	将用户信息放入请求头中，传递给后台使用！

    private AntPathMatcher antPathMatcher=new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        if (antPathMatcher.match("/**/inner/**",path)){
            return out(response, ResultCodeEnum.PERMISSION);
        }
        String userId=getUserId(request);
        String userTempId=getUserTempId(request);
        if ("-1".equals(userId)){
            return out(response, ResultCodeEnum.PERMISSION);
        }
        if (antPathMatcher.match("/api/**/auth/*",path)){
            if (StringUtils.isEmpty(userId)){
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }
        String[] split = authUrls.split(",");
        if (split!=null&&split.length>0){
            for (String url : split) {
                if (path.indexOf(url)!=-1&&StringUtils.isEmpty(userId)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+request.getURI());
                    return response.setComplete();
                }
            }
        }
        if (!StringUtils.isEmpty(userId)||!StringUtils.isEmpty(userTempId)){
            if (!StringUtils.isEmpty(userId)){
                request.mutate().header("userId",userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId",userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempId="";
        HttpCookie httpCookie= request.getCookies().getFirst("userTempId");
        if (httpCookie!=null){
            userTempId=httpCookie.getValue();
        }else {
            List<String> list = request.getHeaders().get("userTempId");
            if (!CollectionUtils.isEmpty(list)){
                userTempId=list.get(0);
            }
        }
        return userTempId;
    }

    private String getUserId(ServerHttpRequest request) {
//        先获得token
        String token="";
        HttpCookie httpCookie= request.getCookies().getFirst("token");
        if (httpCookie!=null){
            token=httpCookie.getValue();
        }else {
            List<String> list = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(list)){
                token=list.get(0);
            }
        }
//        再获得userId
        if (!StringUtils.isEmpty(token)){
            String userKey="user:login:"+token;
            String userJson = (String) redisTemplate.opsForValue().get(userKey);
            if (!StringUtils.isEmpty(userJson)){
                JSONObject userStr = JSONObject.parseObject(userJson);
                String ip = (String) userStr.get("ip");
                if (ip.equals(IpUtil.getGatwayIpAddress(request))){
                    return (String) userStr.get("userId");
                }else {
                    return "-1";
                }
            }
        }
        return "";
    }
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        String resultStr= JSON.toJSONString(result);
        DataBuffer wrap = response.bufferFactory().wrap(resultStr.getBytes());
        response.getHeaders().add("Content-Type","application/json;charset=utf-8");
        return response.writeWith(Mono.just(wrap));
    }
}
