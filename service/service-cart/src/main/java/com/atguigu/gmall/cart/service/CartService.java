package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/18
 * description:
 */
public interface CartService {
    void addToCart(Long skuId, Integer skuNum, String userId);

    List<CartInfo> getCartList(String userId, String userTempId);

    void checkCart(Long skuId, String userId, Integer isChecked);

    void deleteCart(Long skuId, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
