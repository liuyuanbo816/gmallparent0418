package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * title:
 * author: bai
 * date: 2022/10/20
 * description:
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Value("${ware.url}")
    private String wareUrl;

//    从数据库中查询商品详情
    @Override
    public IPage<OrderInfo> getOrderPage(Page<OrderInfo> orderInfoPage, String userId) {
        IPage<OrderInfo> iPage=orderInfoMapper.selectOrderPage(orderInfoPage,userId);
        iPage.getRecords().stream().forEach(orderInfo -> {
            String statusNameByStatus = OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus());
            orderInfo.setOrderStatusName(statusNameByStatus);
        });
        return iPage;
    }

    //保存订单到数据库
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {

        //  total_amount,order_status,user_id,out_trade_no,trade_body,operate_time,expire_time,process_status
        orderInfo.sumTotalAmount();

        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+new Random().nextInt(10000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setTradeBody("毕业礼物");
        orderInfo.setOperateTime(new Date());
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)){
            orderDetailList.stream().forEach(orderDetail -> {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }
        Long orderId=orderInfo.getId();
        return orderId;
    }

    //    判断仓库是否有库存
    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    //    点击保存订单后删除redis中的tradeNo
    @Override
    public void delTradeNo(String userId) {
        String tradeKey="tradeNo:"+userId;
        redisTemplate.delete(tradeKey);
    }

    //    将其与redis存储进行比较
    @Override
    public boolean checkTradeNo(String tradeNo, String userId) {
        String tradeKey="tradeNo:"+userId;
        String tradeNo1 = (String) redisTemplate.opsForValue().get(tradeKey);

        return tradeNo.equals(tradeNo1);
    }

    //    得到交易流水号，并将其存到redis
    @Override
    public String getTradeNo(String userId) {

        String tradeNo= UUID.randomUUID().toString();
        String tradeKey="tradeNo:"+userId;
        redisTemplate.opsForValue().set(tradeKey,tradeNo);
        return tradeNo;
    }
}
