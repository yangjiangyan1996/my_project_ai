package com.example.controller;

import com.example.Facade.QuanFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.QuanBarCreateReq;
import com.example.entity.req.RemoveMemberReq;
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
}
