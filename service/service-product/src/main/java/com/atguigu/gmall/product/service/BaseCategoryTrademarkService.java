package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/4
 * description:
 */
public interface BaseCategoryTrademarkService {
    List<BaseTrademark> getTrademarkList(Long category3Id);

    List<BaseTrademark> getCurrentTrademarkList(Long category3Id);

    void saveCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo);

    void removeCategoryTrademark(Long category3Id, Long trademarkId);
}
