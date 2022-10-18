package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * title:
 * author: bai
 * date: 2022/10/18
 * description:
 */
@Controller
public class CartController {
    @Autowired
    private ProductFeignClient productFeignClient;

//    window.location.href = 'http://cart.gmall.com/addCart.html?skuId=' + this.skuId + '&skuNum=' + this.skuNum + '&sourceType=' + sourceType
    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        request.setAttribute("skuNum",skuNum);
        request.setAttribute("skuInfo",skuInfo);
        return "cart/addCart";
    }
    @GetMapping("cart.html")
    public String cartList(){
        return "cart/index";
    }
}
