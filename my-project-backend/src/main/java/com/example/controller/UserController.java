package com.example.controller;

import com.example.Facade.UserFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Account;
import com.example.entity.resp.MyCountResp;
import com.example.entity.resp.UserAllInfo;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/auth/user")
public class UserController {

    @Resource
    UserFacade userFacade;


    @GetMapping("/getUserAllInfo")
    public RespBean<UserAllInfo> getUserAllInfo() {
        UserInfo user = UserUtil.getCurrentUser();
        UserAllInfo u = userFacade.getUserAllInfo(user.getId());
        return RespBean.success(u);
    }


    @GetMapping("/getCurrentUserInfo")
    public RespBean<UserInfo> getCurrentUserInfo() {
        UserInfo user = UserUtil.getCurrentUser();
        return RespBean.success(user);
    }
}