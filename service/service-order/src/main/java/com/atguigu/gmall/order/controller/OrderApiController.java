package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * title:
 * author: bai
 * date: 2022/10/19
 * description:
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;


    @Autowired
    private OrderService orderService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);

        Map map=new HashMap<>();

        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        //  userAddressList detailArrayList totalNum  totalAmount
        AtomicInteger totalNum = new AtomicInteger();
        List<OrderDetail> detailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            totalNum.addAndGet(cartInfo.getSkuNum());


            return orderDetail;
        }).collect(Collectors.toList());
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        map.put("userAddressList",userAddressList);
        map.put("detailArrayList",detailArrayList);
        map.put("totalNum",totalNum);
        map.put("totalAmount",orderInfo.getTotalAmount());
        map.put("tradeNo",orderService.getTradeNo(userId));
        return Result.ok(map);
    }

//    将订单信息封装到数据库中
    @PostMapping("auth/submitOrder")
    public Result saveOrderInfo(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);

        orderInfo.setUserId(Long.parseLong(userId));
        String tradeNo = request.getParameter("tradeNo");

        boolean result=orderService.checkTradeNo(tradeNo,userId);
        if (!result){
            return Result.fail().message("不能重复无刷新回退提交订单");
        }
        orderService.delTradeNo(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

//        准备线程集合
        List<CompletableFuture> completableFutureList=new ArrayList<>();
//        错误信息集合
        List<String> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderDetailList)){
            for (OrderDetail orderDetail : orderDetailList) {

                CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                    boolean exist = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                    if (!exist) {
                        errorList.add(orderDetail.getSkuId() + ":库存不足");
                    }
                }, threadPoolExecutor);
                completableFutureList.add(stockCompletableFuture);

                CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    BigDecimal orderPrice = orderDetail.getOrderPrice();
                    if (skuPrice.compareTo(orderPrice)!=0){
                        String msg=skuPrice.compareTo(orderPrice)==1?"涨价":"降价";
//                    更新购物车价格，修改redis
                        String userCartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(userCartKey, orderDetail.getSkuId().toString());
                        cartInfo.setSkuPrice(skuPrice);
                        redisTemplate.opsForHash().put(userCartKey,orderDetail.getSkuId().toString(),cartInfo);
                        errorList.add(orderDetail.getSkuId() + msg+skuPrice.subtract(orderPrice).abs());
                    }
                }, threadPoolExecutor);
                completableFutureList.add(priceCompletableFuture);
            }
        }

        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();

        if (errorList.size()>0){
            return Result.fail().message(StringUtils.join(errorList,","));
        }
        Long orderId=orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

//      url: this.api_name + `/auth/${page}/${limit}`,
//    我的订单页面
    @GetMapping("/auth/{page}/{limit}")
    public Result getOrderPage(@PathVariable Long page,
                               @PathVariable Long limit,
                               HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        IPage<OrderInfo> iPage=orderService.getOrderPage(orderInfoPage,userId);
        return Result.ok(iPage);
    }


}
