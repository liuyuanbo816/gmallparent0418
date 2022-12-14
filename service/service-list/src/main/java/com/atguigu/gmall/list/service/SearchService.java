package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

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

    SearchResponseVo search(SearchParam searchParam);
}
