package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * title:
 * author: bai
 * date: 2022/10/4
 * description:
 */
@Service
public class BaseCategoryTrademarkServiceImpl implements BaseCategoryTrademarkService {
    @Resource
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public List<BaseTrademark> getTrademarkList(Long category3Id) {
        //获取关联list
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(new QueryWrapper<BaseCategoryTrademark>().eq("category3_id", category3Id));
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> tmIdList = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectBatchIds(tmIdList);
            return baseTrademarkList;
        }
        return null;
    }

    @Override
    public void saveCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo) {
        Long category3Id = categoryTrademarkVo.getCategory3Id();
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            trademarkIdList.forEach(trademarkId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(category3Id);
                baseCategoryTrademark.setTrademarkId(trademarkId);
                baseCategoryTrademarkMapper.insert(baseCategoryTrademark);
            });
        }
    }

    @Override
    public void removeCategoryTrademark(Long category3Id, Long trademarkId) {
        baseCategoryTrademarkMapper.delete(new QueryWrapper<BaseCategoryTrademark>().eq("category3_id", category3Id).eq("trademark_id", trademarkId));
    }

    @Override
    public List<BaseTrademark> getCurrentTrademarkList(Long category3Id) {
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(new QueryWrapper<BaseCategoryTrademark>().eq("category3_id", category3Id));
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> tmIdList = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null).stream().filter(baseTrademark -> {
                return !tmIdList.contains(baseTrademark.getId());
            }).collect(Collectors.toList());
            return baseTrademarkList;
        } else {
            return baseTrademarkMapper.selectList(null);
        }
    }


}
