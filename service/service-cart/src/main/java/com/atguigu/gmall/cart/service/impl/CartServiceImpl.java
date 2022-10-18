package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/18
 * description:
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void checkCart(Long skuId, String userId, Integer isChecked) {
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        CartInfo cartInfo = boundHashOperations.get(skuId.toString());
        if (cartInfo!=null){
            cartInfo.setIsChecked(isChecked);
            cartInfo.setUpdateTime(new Date());
        }
        boundHashOperations.put(skuId.toString(),cartInfo);
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        boundHashOperations.delete(skuId.toString());
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {

        List<CartInfo> cartInfoLoginList=new ArrayList<>();
        List<CartInfo> cartInfoNoLoginList=new ArrayList<>();
//查找临时用户购物车
        if (!StringUtils.isEmpty(userTempId)){

            String cartKey = getCartKey(userTempId);
            cartInfoNoLoginList = redisTemplate.opsForHash().values(cartKey);
        }
//        购物车不为空且userId为空
        if (!CollectionUtils.isEmpty(cartInfoNoLoginList)&&StringUtils.isEmpty(userId)){

            cartInfoNoLoginList.sort((o1,o2)->{
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(),o1.getUpdateTime(), Calendar.SECOND);
            });
            return cartInfoNoLoginList;
        }
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(getCartKey(userId));


        if (!CollectionUtils.isEmpty(cartInfoNoLoginList)){
            for (CartInfo cartInfo : cartInfoNoLoginList) {
                if (boundHashOperations.hasKey(cartInfo.getSkuId().toString())){
                    CartInfo cartInfo1 = boundHashOperations.get(cartInfo.getSkuId().toString());
                    cartInfo1.setUpdateTime(new Date());
                    cartInfo1.setSkuNum(cartInfo.getSkuNum()+cartInfo1.getSkuNum());
                    if (cartInfo.getIsChecked()==1){
                        cartInfo1.setIsChecked(1);
                    }
                    boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo1);
                }else {
                    cartInfo.setUserId(userId);
                    cartInfo.setCreateTime(new Date());
                    cartInfo.setUpdateTime(new Date());
                    boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
                }
            }
            redisTemplate.delete(getCartKey(userTempId));
        }
        cartInfoLoginList = boundHashOperations.values();
        if (CollectionUtils.isEmpty(cartInfoLoginList)){
            return new ArrayList<>();
        }
        cartInfoLoginList.sort((o1,o2)->{
            return DateUtil.truncatedCompareTo(o2.getUpdateTime(),o1.getUpdateTime(), Calendar.SECOND);
        });
        return cartInfoLoginList;
    }

    @Override
    public void addToCart(Long skuId, Integer skuNum, String userId) {
        String cartKey=getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOperations.get(skuId.toString());
        if (cartInfo!=null){
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        }else {
            cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setUpdateTime(new Date());
            cartInfo.setCreateTime(new Date());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
        }
        boundHashOperations.put(skuId.toString(),cartInfo);
    }

    private String getCartKey(String userId) {

        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }
}
