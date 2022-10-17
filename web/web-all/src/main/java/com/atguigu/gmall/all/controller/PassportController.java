package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * title:
 * author: bai
 * date: 2022/10/17
 * description:
 */
@Controller
public class PassportController {

    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "login";
    }

}
