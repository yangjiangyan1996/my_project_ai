package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.MessageFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.MsgOfCommentListPageReq;
import com.example.entity.req.SearchUserReq;
import com.example.entity.resp.MsgOfCommentListResp;
import com.example.entity.resp.UserSearchResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 16:23
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/msg/")
public class MessageContorller {
    @Resource
    MessageFacade messageFacade;

    @PostMapping("/msgOfCommentList")
    public RespBean<Page<MsgOfCommentListResp>> msgOfCommentList(@RequestBody MsgOfCommentListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MsgOfCommentListResp> list = messageFacade.msgOfCommentList(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#msgOfCommentList,req:{}", req,e);
            return RespBean.failure(999, e.getMessage());
        }  catch (Exception e) {
            log.error("MessageContorller#msgOfCommentList, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
