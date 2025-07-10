package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.MessageFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.MarkMsgAsReadReq;
import com.example.entity.req.MsgOfCommentListPageReq;
import com.example.entity.req.MsgOfFollowedPageReq;
import com.example.entity.req.MsgOfLikedPageReq;
import com.example.entity.resp.MsgOfCommentListResp;
import com.example.entity.resp.MsgOfFollowerResp;
import com.example.entity.resp.MsgOfLikedPageResp;
import com.example.enums.MessageEnums;
import com.example.filter.UserUtil;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @GetMapping("/unreadMsgCount")
    public RespBean<Long> unreadMsgCount() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long list = messageFacade.unreadMsgCount(user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#unreadMsgCount,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#unreadMsgCount, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/markMsgAsRead")
    public RespBean<Boolean> markMsgAsRead(@RequestBody MarkMsgAsReadReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean list = messageFacade.markMsgAsRead(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#markMsgAsRead,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#markMsgAsRead, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    @GetMapping("/allMarkRead")
    public RespBean<Boolean> allMarkRead(@RequestParam(value = "type") String type) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<Integer> types = new ArrayList<>();
            if ("comment".equals(type)) {
                types = LettuceLists.newList(MessageEnums.MessageType.REPLY_PROJECT.getCode(), MessageEnums.MessageType.REPLY_COMMENT.getCode());
            } else if ("follow".equals(type)) {
                types = LettuceLists.newList(MessageEnums.MessageType.PUBLISHER.getCode());
            } else if ("like".equals(type)) {
                types = LettuceLists.newList(MessageEnums.MessageType.LIKE_POST.getCode(), MessageEnums.MessageType.LIKE_COMMENT.getCode());
            }
            Boolean list = messageFacade.allMarkRead(types, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#allMarkRead,req:{}", type, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#allMarkRead, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/msgOfFollowed")
    public RespBean<Page<MsgOfFollowerResp>> msgOfFollowed(@RequestBody MsgOfFollowedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MsgOfFollowerResp> list = messageFacade.msgOfFollowed(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#msgOfFollowed,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#msgOfFollowed, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/msgOfCommentList")
    public RespBean<Page<MsgOfCommentListResp>> msgOfCommentList(@RequestBody MsgOfCommentListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MsgOfCommentListResp> list = messageFacade.msgOfCommentList(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#msgOfCommentList,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#msgOfCommentList, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/msgOfLiked")
    public RespBean<Page<MsgOfLikedPageResp>> msgOfLiked(@RequestBody MsgOfLikedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MsgOfLikedPageResp> list = messageFacade.msgOfLiked(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("MessageContorller#msgOfLiked,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("MessageContorller#msgOfLiked, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
