package com.keymao.security.springboot.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    @RequestMapping(value = "/login-success",produces = {"text/plain;charset=UTF-8"})
    public String loginSuccess(){
        String username = getUsername();
        return username + " 登录成功";
    }

    /**
     * 测试资源1
     * @return
     */
    @GetMapping(value = "/r/r1",produces = {"text/plain;charset=UTF-8"})
    public String r1(){
        return getUsername() + " 访问资源1";
    }

    /**
     * 测试资源2
     * @return
     */
    @GetMapping(value = "/r/r2",produces = {"text/plain;charset=UTF-8"})
    public String r2(){
        return getUsername() + " 访问资源2";
    }

    /**
     * 获取当前登录用户名
     * @return
     */
    private String getUsername(){
        //当前认证通过的用户身份
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.isAuthenticated()){
            return "匿名";
        }
        //用户身份
        Object principal = authentication.getPrincipal();
        String username = null;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }
}
