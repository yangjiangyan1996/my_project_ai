package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.QuanFacade;
import com.example.Facade.TaskFacade;
import com.example.Facade.TieFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.*;
import com.example.entity.resp.AchievementBaseInfoResp;
import com.example.entity.resp.AchievementMyPageResp;
import com.example.entity.resp.BarsInfoResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/18 17:38
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/achievement/")
public class AchievementController {

    @Resource
    CommonFacade commonFacade;
    @Resource
    TaskFacade taskFacade;


    /**
     * 获取用户收藏的圈
     * @param req
     * @return
     */
    @PostMapping("/myAchievementPageList")
    public RespBean<Page<AchievementMyPageResp>> myAchievementPageList(@RequestBody AchievementMyPageReq req) {
        try {
            Long userId;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            if (userId == null) {
                return new RespBean<>();
            }
            req.setUserId(userId);
            Page<AchievementMyPageResp> result = taskFacade.myAchievementPageList(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("AchievementController#myAchievementPageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("AchievementController#myAchievementPageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    @GetMapping("/getMyAchievementBaseInfo")
    public RespBean<AchievementBaseInfoResp> getMyAchievementBaseInfo() {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            AchievementBaseInfoResp result = taskFacade.getMyAchievementBaseInfo(userId);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("AchievementController#getMyAchievementBaseInfo", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("AchievementController#getMyAchievementBaseInfo", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}
