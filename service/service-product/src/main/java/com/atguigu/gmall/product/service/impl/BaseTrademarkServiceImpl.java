package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * title:
 * author: bai
 * date: 2022/10/4
 * description:
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper,BaseTrademark> implements BaseTrademarkService {
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;
    @Override
    public IPage getBaseTrademarkList(Page<BaseTrademark> baseTrademarkPage) {
        return baseTrademarkMapper.selectPage(baseTrademarkPage,new QueryWrapper<BaseTrademark>().orderByDesc("id"));
    }

}
