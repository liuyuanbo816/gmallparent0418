package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/10/18
 * description:
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

//    url: this.api_name + '/addToCart/' + skuId + '/' + skuNum,
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){

        String userId= AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId=AuthContextHolder.getUserTempId(request);
        }
        cartService.addToCart(skuId,skuNum,userId);
        return Result.ok();
    }

//          url: this.api_name + '/cartList',
    @GetMapping("cartList")
    public Result getCartList(HttpServletRequest request){
        String userId= AuthContextHolder.getUserId(request);
        String userTempId=AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList=cartService.getCartList(userId,userTempId);
        return Result.ok(cartInfoList);
    }
//          url: this.api_name + '/checkCart/' + skuId + '/' + isChecked,
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){
        String userId= AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId=AuthContextHolder.getUserTempId(request);
        }
        cartService.checkCart(skuId,userId,isChecked);
        return Result.ok();
    }
//          url: this.api_name + '/deleteCart/' + skuId,
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request){
        String userId= AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId=AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId,userId);
        return Result.ok();
    }

}
