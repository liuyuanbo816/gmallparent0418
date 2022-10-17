package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * title:
 * author: bai
 * date: 2022/10/17
 * description:
 */
@RestController
@RequestMapping("api/user/passport")
public class PassportApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
        UserInfo info = userService.login(userInfo);
        if (info!=null){
//            登录成功，将信息存到redis中，前端存token
            HashMap<String, Object> hashMap = new HashMap<>();
            String token = UUID.randomUUID().toString();
            hashMap.put("token",token);
            hashMap.put("nickName",info.getNickName());
            String redisKey=RedisConst.USER_LOGIN_KEY_PREFIX+token;

            String ipAddress = IpUtil.getIpAddress(request);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",info.getId().toString());
            jsonObject.put("ip",ipAddress);

            redisTemplate.opsForValue().set(redisKey,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(hashMap);
        }else {
            return Result.fail().message("用户名或密码失败！请注册");
        }
    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request,@RequestHeader String token){
        String redisKey=RedisConst.USER_KEY_PREFIX+token;
        redisTemplate.delete(redisKey);
        return Result.ok();
    }

}
