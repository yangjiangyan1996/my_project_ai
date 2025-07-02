package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.MyFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.MyFollowCountResp;
import com.example.entity.resp.MyPublishedResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:47
 */
@RestController
@RequestMapping("/api/auth/my/")
@Slf4j
public class MyController {
    @Resource
    private MyFacade myFacade;

    @PostMapping("/myFollowCount")
    public RespBean<MyFollowCountResp> myFollowCount() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            MyFollowCountResp hotFuyeProjects = myFacade.myFollowCount(user.getId());
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myLike")
    public RespBean<Page<MyPublishedResp>> myLike(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyPublishedResp> hotFuyeProjects = myFacade.myLike(req, user.getId());
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/myFavorites")
    public RespBean<Page<MyPublishedResp>> myFavorites(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyPublishedResp> hotFuyeProjects = myFacade.myFavorites(req, user.getId());
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myPublished")
    public RespBean<Page<MyPublishedResp>> myPublished(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyPublishedResp> list = myFacade.myPublished(req, user.getId());
            return RespBean.success(list);
        } catch (Exception e) {
            log.error("Mycontroller#myPublished, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
