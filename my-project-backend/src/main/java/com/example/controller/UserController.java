package com.example.controller;

import com.example.Facade.CommonFacade;
import com.example.Facade.UserFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.UpdateUserInfoReq;
import com.example.entity.resp.UserAllInfo;
import com.example.enums.CommonEnum;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth/user")
public class UserController {

    @Resource
    CommonFacade commonFacade;
    @Resource
    UserFacade userFacade;


    @PostMapping("/updateUserInfo")
    public RespBean<Boolean> updateUserInfo(@RequestBody UpdateUserInfoReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean u = userFacade.updateUserInfo(req);
            return RespBean.success(u);
        } catch (ValidationException e) {
            log.error("ProjectController#updateUserInfo,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#updateUserInfo,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/getUserAllInfo")
    public RespBean<UserAllInfo> getUserAllInfo() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            UserAllInfo u = userFacade.getUserAllInfo(user.getId());
            return RespBean.success(u);
        } catch (ValidationException e) {
            log.error("ProjectController#getUserAllInfo,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#getUserAllInfo,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/getCurrentUserInfo")
    public RespBean<UserInfo> getCurrentUserInfo() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            return RespBean.success(user);
        } catch (ValidationException e) {
            log.error("ProjectController#getCurrentUserInfo,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#getCurrentUserInfo,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/getSecrecyIdUserInfo")
    public RespBean<UserInfo> getCurrentUserInfo(@RequestParam(value = "secrecyId", required = false) Long secrecyId) {
        try {
            Long userIdBySecrecyId = commonFacade.getUserIdBySecrecyId(secrecyId);
            UserAllInfo userAllInfo = userFacade.getUserAllInfo(userIdBySecrecyId);
            UserInfo user = new UserInfo();
            user.setUsername(userAllInfo.getUsername());
            user.setIndustryName(CommonEnum.IndustryEnum.getByCode(userAllInfo.getIndustryCode()));
            user.setAvatarUrl(userAllInfo.getAvatarUrl());
            return RespBean.success(user);
        } catch (ValidationException e) {
            log.error("ProjectController#getCurrentUserInfo,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#getCurrentUserInfo,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}