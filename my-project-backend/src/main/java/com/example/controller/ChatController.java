package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.ChatFacade;
import com.example.Facade.CommonFacade;
import com.example.Facade.UserFacade;
import com.example.entity.base.RespBean;
import com.example.entity.req.ChatCreateMessageReq;
import com.example.entity.req.ChatHistoryPageReq;
import com.example.entity.req.ChatNewMessagesReq;
import com.example.entity.req.ChatCheckReadStatusReq;
import com.example.entity.resp.ChatCreateMessageResp;
import com.example.entity.resp.ChatHistoryResp;
import com.example.entity.resp.ChatCheckReadStatusResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     *  发送消息
     * @param req
     * @return 会话ID
     */
    @PostMapping("/sendMessage")
    public RespBean<ChatCreateMessageResp> sendMessage(@Validated @RequestBody ChatCreateMessageReq req) {
        try {
            req.setCurrentUserId(UserUtil.getCurrentUser().getId());
            ChatCreateMessageResp result = chatFacade.sendMessage(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ChatController#sendMessage,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ChatController#sendMessage,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 获取聊天记录
     * @param req
     * @return
     */
    @PostMapping("/getChatHistory")
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

    /**
     * 获取最新消息
     * @param req
     * @return
     */
    @PostMapping("/getNewMessages")
    public RespBean<List<ChatHistoryResp>> getNewMessages(@Validated @RequestBody ChatNewMessagesReq req) {
        try {
            req.setCurrentUserId(UserUtil.getCurrentUser().getId());
            List<ChatHistoryResp> result = chatFacade.getNewMessages(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ChatController#getNewMessages,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ChatController#getNewMessages,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
    
    /**
     * 检查消息已读状态
     * @param req
     * @return
     */
    @PostMapping("/checkReadStatus")
    public RespBean<ChatCheckReadStatusResp> checkReadStatus(@Validated @RequestBody ChatCheckReadStatusReq req) {
        try {
            ChatCheckReadStatusResp result = chatFacade.checkReadStatus(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ChatController#checkReadStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ChatController#checkReadStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}