package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.req.MarkMsgAsReadReq;
import com.example.entity.req.MsgOfCommentListPageReq;
import com.example.entity.req.MsgOfFollowedPageReq;
import com.example.entity.req.MsgOfLikedPageReq;
import com.example.entity.resp.MsgOfCommentListResp;
import com.example.entity.resp.MsgOfFollowerResp;
import com.example.entity.resp.MsgOfLikedPageResp;
import com.example.enums.MessageEnums;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/9 11:13
 */
@Service
@Slf4j
public class MessageFacade {
    @Resource
    AccountService accountService;
    @Resource
    MessagesService messagesService;
    @Resource
    MessageUserSettingsService messageUserSettingsService;
    @Resource
    ProjectCommentService projectCommentService;
    @Resource
    UserFollowService userFollowService;
    @Resource
    ProjectService projectService;

    public void createMessageOfPublisher(Long targetId, Long currentUserId) {
        messagesService.createNotification(targetId, currentUserId, MessageEnums.MessageType.PUBLISHER.getCode(), "关注了您", null, null);
    }

    public void deletedMessageOfPublisher(Long targetId, Long currentUserId) {
        messagesService.deletedNotification(targetId, currentUserId, MessageEnums.MessageType.PUBLISHER.getCode(), null);
    }

    public void deletedMessageOfLike(Long projectId, Long relatedId, Long currentUserId) {
        Long receiverId = 0L; // 获取接收者id
        Integer type = null;
        Projects p = projectService.selectByProjectId(projectId);

        if (relatedId != null) {
            ProjectComment pc = projectCommentService.selectById(relatedId);
            type = MessageEnums.MessageType.LIKE_COMMENT.getCode();
            receiverId = pc.getUserId();
        } else {
            receiverId = p.getCreatedBy();
            type = MessageEnums.MessageType.LIKE_POST.getCode();
        }
        if (Objects.equals(receiverId, currentUserId)) {
            log.info("MessageFacade#createMessageOfLike，消息接收者与发送者相同，不发送消息,id:{}", currentUserId);
            return;
        }
        messagesService.deletedNotification(receiverId, currentUserId, type, relatedId);
    }
    public void createMessageOfLike(Long projectId, Long relatedId, Long currentUserId) {
        Long receiverId = 0L; // 获取接收者id
        Integer type = null;
        String content = null;
        String relatedWords = null;
        Projects p = projectService.selectByProjectId(projectId);

        if (relatedId != null) {
            ProjectComment pc = projectCommentService.selectById(relatedId);
            type = MessageEnums.MessageType.LIKE_COMMENT.getCode();
            content = "点赞了您的评论";
            receiverId = pc.getUserId();
            relatedWords = pc.getContent();
        } else {
            content = "点赞了您的项目";
            receiverId = p.getCreatedBy();
            type = MessageEnums.MessageType.LIKE_POST.getCode();
            relatedWords = p.getName();
        }
        if (Objects.equals(receiverId, currentUserId)) {
            log.info("MessageFacade#createMessageOfLike，消息接收者与发送者相同，不发送消息,id:{}", currentUserId);
            return;
        }
        messagesService.createNotification(receiverId, currentUserId, type, content, relatedId, relatedWords);
    }

    public void createMessageOfComment(Long projectId, Long relatedId, Long replyTo, Long currentUserId) {
        Long receiverId = null; // 获取接收者id
        String content = null;
        Integer type = null;
        String relatedWords = null;
        Projects p = projectService.selectByProjectId(projectId);
        if (replyTo != null) {
            ProjectComment pc = projectCommentService.selectById(replyTo);
            if (pc != null) {
                receiverId = pc.getUserId();
                content = "回复了您的评论";
                type = MessageEnums.MessageType.REPLY_COMMENT.getCode();
                relatedWords = pc.getContent();
            }
        } else if (projectId != null) {
            receiverId = p.getCreatedBy();
            content = "评论了您的项目";
            type = MessageEnums.MessageType.REPLY_PROJECT.getCode();
            relatedWords = p.getName();
        }

        if (Objects.equals(receiverId, currentUserId)) {
            log.info("MessageFacade#createMessageOfComment，消息接收者与发送者相同，不发送消息,id:{}", currentUserId);
            return;
        }
        messagesService.createNotification(receiverId, currentUserId, type, content, relatedId, relatedWords);
    }


    public Page<MsgOfFollowerResp> msgOfFollowed(MsgOfFollowedPageReq req, Long userId) {
        List<Integer> types = LettuceLists.newList(MessageEnums.MessageType.PUBLISHER.getCode());
        Page<Messages> list = messagesService.getPage(Page.of(req.getPage() - 1, req.getSize()), userId, types);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> userIds = list.getRecords().stream().map(v -> v.getSenderId()).distinct().collect(Collectors.toList());
        //查询发送者是否当前用户已经关注

        List<UserFollow> userFollows = userFollowService.selectByFollowerId(userId);
        List<Long> userFollowerIds = userFollows.stream()
                .filter(v -> userIds.contains(v.getFolloweeId()))
                .map(UserFollow::getFolloweeId).distinct()
                .collect(Collectors.toList());

        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        List<MsgOfFollowerResp> collect = list.getRecords().stream().map(v -> {
                    MsgOfFollowerResp projectsResp = new MsgOfFollowerResp();
                    BeanUtils.copyProperties(v, projectsResp);
                    if (!CollectionUtils.isEmpty(userFollowerIds)) {
                        if (userFollowerIds.contains(v.getSenderId())) {
                            projectsResp.setNeedFollow(Boolean.FALSE);
                        } else {
                            projectsResp.setNeedFollow(Boolean.TRUE);
                        }
                    } else {
                        projectsResp.setNeedFollow(Boolean.TRUE);
                    }

                    if (!CollectionUtils.isEmpty(userId2UserInfoMap) && userId2UserInfoMap.containsKey(v.getSenderId())) {
                        projectsResp.setSenderName(userId2UserInfoMap.get(v.getSenderId()).getNickname());
                        projectsResp.setSenderAvatar(userId2UserInfoMap.get(v.getSenderId()).getAvatarUrl());
                    }

                    projectsResp.setSecrecyId(userId2UserInfoMap.get(v.getSenderId()).getSecrecyId());
                    return projectsResp;
                })
                .sorted(Comparator.comparing(MsgOfFollowerResp::getIsRead)
                        .thenComparing(MsgOfFollowerResp::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        Page<MsgOfFollowerResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }


    public Page<MsgOfCommentListResp> msgOfCommentList(MsgOfCommentListPageReq req, Long userId) {
        List<Integer> types = LettuceLists.newList(MessageEnums.MessageType.REPLY_PROJECT.getCode(), MessageEnums.MessageType.REPLY_COMMENT.getCode());
        Page<Messages> list = messagesService.getPage(Page.of(req.getPage() - 1, req.getSize()), userId, types);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        Map<Long, Account> userId2UserInfoMap = new HashMap<>();
        List<Long> userIds = list.getRecords().stream().map(v -> v.getSenderId()).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(userIds)) {
            List<Account> accounts = accountService.selectByIds(userIds);
            userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        }

        Map<Long, ProjectComment> commentMap = new HashMap<>();
        List<Long> replyCommentIds = list.getRecords().stream().map(v -> v.getRelatedId()).distinct().collect(Collectors.toList());
        List<ProjectComment> commentList = projectCommentService.selectByIds(replyCommentIds);
        if (commentList != null) {
            commentMap = commentList.stream().collect(Collectors.toMap(ProjectComment::getId, v -> v));
        }


        Map<Long, Account> finalUserId2UserInfoMap = userId2UserInfoMap;
        Map<Long, ProjectComment> finalCommentMap = commentMap;
        List<MsgOfCommentListResp> collect = list.getRecords().stream().map(v -> {
                    MsgOfCommentListResp projectsResp = new MsgOfCommentListResp();
                    BeanUtils.copyProperties(v, projectsResp);

                    if (!CollectionUtils.isEmpty(finalUserId2UserInfoMap) && finalUserId2UserInfoMap.containsKey(v.getSenderId())) {
                        projectsResp.setSenderName(finalUserId2UserInfoMap.get(v.getSenderId()).getNickname());
                    }
                    projectsResp.setProjectId(finalCommentMap.get(v.getRelatedId()).getProjectId());
                    projectsResp.setCommentId(v.getRelatedId());
                    return projectsResp;
                })
                .sorted(Comparator.comparing(MsgOfCommentListResp::getIsRead)
                        .thenComparing(MsgOfCommentListResp::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        Page<MsgOfCommentListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<MsgOfLikedPageResp> msgOfLiked(MsgOfLikedPageReq req, Long userId) {
        List<Integer> types = LettuceLists.newList(MessageEnums.MessageType.LIKE_POST.getCode(), MessageEnums.MessageType.LIKE_COMMENT.getCode());
        Page<Messages> list = messagesService.getPage(Page.of(req.getPage() - 1, req.getSize()), userId, types);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        Map<Long, ProjectComment> commentMap = new HashMap<>();
        List<Long> replyCommentIds = list.getRecords().stream().map(v -> v.getRelatedId()).distinct().collect(Collectors.toList());
        List<ProjectComment> commentList = projectCommentService.selectByIds(replyCommentIds);
        if (commentList != null) {
            commentMap = commentList.stream().collect(Collectors.toMap(ProjectComment::getId, v -> v));
        }

        List<Long> userIds = list.getRecords().stream().map(v -> v.getSenderId()).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        Map<Long, ProjectComment> finalCommentMap = commentMap;
        List<MsgOfLikedPageResp> collect = list.getRecords().stream().map(v -> {
                    MsgOfLikedPageResp projectsResp = new MsgOfLikedPageResp();
                    BeanUtils.copyProperties(v, projectsResp);

                    if (!CollectionUtils.isEmpty(userId2UserInfoMap) && userId2UserInfoMap.containsKey(v.getSenderId())) {
                        projectsResp.setSenderName(userId2UserInfoMap.get(v.getSenderId()).getNickname());
                    }
                    projectsResp.setProjectId(finalCommentMap.get(v.getRelatedId()).getProjectId());
                    projectsResp.setCommentId(v.getRelatedId());
                    return projectsResp;
                })
                .sorted(Comparator.comparing(MsgOfLikedPageResp::getIsRead)
                        .thenComparing(MsgOfLikedPageResp::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        Page<MsgOfLikedPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Long unreadMsgCount(Long userId) {
        List<Integer> types = LettuceLists.newList(
                MessageEnums.MessageType.REPLY_PROJECT.getCode(),
                MessageEnums.MessageType.REPLY_COMMENT.getCode(),
                MessageEnums.MessageType.PUBLISHER.getCode(),
                MessageEnums.MessageType.LIKE_POST.getCode(),
                MessageEnums.MessageType.LIKE_COMMENT.getCode());


        return messagesService.unreadMsgCount(userId, types);
    }

    public Boolean markMsgAsRead(MarkMsgAsReadReq req, Long userId) {
        return messagesService.markMsgAsRead(req.getId(), userId);
    }

    public Boolean allMarkRead(List<Integer> types, Long userId) {
        if (CollectionUtils.isEmpty(types)) {
            return false;
        }
        return messagesService.allMarkRead(types, userId) > 0;
    }

}
