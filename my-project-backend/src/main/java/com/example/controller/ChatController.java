package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.ChatFacade;
import com.example.Facade.CommonFacade;
import com.example.Facade.UserFacade;
import com.example.entity.base.RespBean;
import com.example.entity.req.ChatHistoryPageReq;
import com.example.entity.resp.ChatHistoryResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/auth/chat")
public class ChatController {

    @Resource
    ChatFacade chatFacade;
    @Resource
    CommonFacade commonFacade;
    @Resource
    UserFacade userFacade;

    @GetMapping("/getChatHistory")
    public RespBean<Page<ChatHistoryResp>> getChatHistory(@RequestBody ChatHistoryPageReq req) {
        try {
            if (req.getSecrecyId() != null) {
                Long userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
                req.setTargetUserId(userId);
            }
            if (req.getTargetUserId() == null) {
                return new RespBean<>();
            }

            req.setCurrentUserId(UserUtil.getCurrentUser().getId());
            Page<ChatHistoryResp> result = chatFacade.getChatHistory(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ChatController#getChatHistory,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ChatController#getChatHistory,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}