package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/19
 * description:
 */
public interface UserAddressService {
    List<UserAddress> getUserAddressListByUserId(String userId);
}
