package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * title:
 * author: bai
 * date: 2022/10/20
 * description:
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    IPage<OrderInfo> selectOrderPage(Page<OrderInfo> orderInfoPage, String userId);
}
