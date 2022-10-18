package com.atguigu.gmall.user.service.impl;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

/**
 * title:
 * author: bai
 * date: 2022/10/17
 * description:
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    public UserInfo login(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        userInfo.setEmail(userInfo.getLoginName());
        userInfo.setPhoneNum(userInfo.getLoginName());
        queryWrapper.and(wrapper->wrapper.eq("login_name",userInfo.getLoginName()).
                or().eq("phone_num",userInfo.getPhoneNum()).or().eq("email",userInfo.getEmail()));
        String passwd = null;
        try {
            passwd = MD5Utils.md5Hex(userInfo.getPasswd().getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        queryWrapper.eq("passwd",passwd);
        UserInfo info = userInfoMapper.selectOne(queryWrapper);
        if (info!=null){
            return info;
        }
        return null;
    }
}
