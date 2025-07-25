package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.example.Facade.QuanFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.BarFollowReq;
import com.example.entity.req.QuanBarCreateReq;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/18 17:38
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/quan/")
public class QuanController {

    @Resource
    private QuanFacade quanFacade;

    /**
     * 创建圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/createBar")
    public RespBean<Boolean> createBar(@RequestBody @Valid QuanBarCreateReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = quanFacade.createBar(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#create,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#create,req:{}", req, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 关注圈子成员
     *
     * @param req
     * @return
     */
    @PostMapping("/followBar")
    public RespBean<Boolean> followBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.followBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#followBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#followBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 取消关注圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/unFollowBar")
    public RespBean<Boolean> unFollowBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.unFollowBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#unFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#unFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 判断用户是否关注圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/isFollowBar")
    public RespBean<Boolean> isFollowBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.isFollowBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#isFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#isFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
