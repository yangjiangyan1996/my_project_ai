package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.MyFacade;
import com.example.entity.base.RespBean;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.MyFollowCountResp;
import com.example.entity.resp.MyPublishedResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    CommonFacade commonFacade;
    @Resource
    private MyFacade myFacade;

    @GetMapping("/myFollowCount")
    public RespBean<MyFollowCountResp> myFollowCount(@RequestParam(value = "secrecyId", required = false) Long secrecyId) {
        try {
            Long userId = null;
            if (secrecyId != null) {
                userId = commonFacade.getUserIdBySecrecyId(secrecyId);
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            MyFollowCountResp hotFuyeProjects = myFacade.myFollowCount(userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myLike")
    public RespBean<Page<MyPublishedResp>> myLike(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            Page<MyPublishedResp> hotFuyeProjects = myFacade.myLike(req, userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/myFavorites")
    public RespBean<Page<MyPublishedResp>> myFavorites(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyPublishedResp> hotFuyeProjects = myFacade.myFavorites(req, userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myPublished")
    public RespBean<Page<MyPublishedResp>> myPublished(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyPublishedResp> list = myFacade.myPublished(req, userId);
            return RespBean.success(list);
        } catch (Exception e) {
            log.error("Mycontroller#myPublished, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
