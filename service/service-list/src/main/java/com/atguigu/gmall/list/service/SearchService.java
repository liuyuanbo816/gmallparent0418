package com.atguigu.gmall.list.service;

/**
 * title:
 * author: bai
 * date: 2022/10/14
 * description:
 */
public interface SearchService {
    void upperGoods(Long skuId);
    void lowerGoods(Long skuId);

    void incrHotScore(Long skuId);
}
