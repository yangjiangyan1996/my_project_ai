package com.example.controller;

import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.filter.UserUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/user")
public class UserController {

    @GetMapping("/getCurrentUserInfo")
    public RespBean<UserInfo> getCurrentUserInfo() {
        UserInfo user = UserUtil.getCurrentUser();
        return RespBean.success(user);
    }
}