package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.req.QuanTieCommentPageReq;
import com.example.entity.req.QuanTieCommentReq;
import com.example.entity.req.QuanTieCreateReq;
import com.example.entity.req.QuanTieListPageReq;
import com.example.entity.resp.*;
import com.example.enums.ProjectEnum;
import com.example.enums.QuanEnum;
import com.example.service.AccountService;
import com.example.service.QuanBarTieService;
import com.example.service.QuanTieCommentLikeService;
import com.example.service.QuanTieCommentService;
import com.example.utils.DateUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:42
 */
@Service
public class TieFacade {
    @Resource
    MessageFacade messageFacade;
    @Resource
    QuanTieCommentLikeService quanTieCommentLikeService;
    @Resource
    QuanTieCommentService quanTieCommentService;
    @Resource
    AccountService accountService;
    @Resource
    private QuanBarTieService quanBarTieService;

    public Boolean createTie(QuanTieCreateReq req, Long userId) {
        QuanBarTie e = new QuanBarTie();
        e.setTitle(req.getTitle());
        e.setContent(req.getContent());
        e.setAvatar(req.getAvatar());
        e.setStatus(QuanEnum.TieStatusEnums.NORMAL.getCode());
        e.setBarId(req.getBarId());
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        return quanBarTieService.save(e);
    }

    public Page<QuanTieListPageResp> getTiePageOfBar(QuanTieListPageReq req) {
        Page<QuanBarTie> page = quanBarTieService.getTiePageOfBar(Page.of(req.getPage(), req.getSize()), req);
        if (page.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<Long> userIds = page.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<QuanTieListPageResp> collect = page.getRecords().stream().map(v -> {
            QuanTieListPageResp r = new QuanTieListPageResp();
            r.setId(v.getId());
            r.setTitle(v.getTitle());
            r.setAvatar(v.getAvatar());
            r.setCreatedName(userId2UserInfoMap.get(v.getCreatedBy()).getNickname());
            r.setCreatedTime(DateUtils.date2Str(v.getCreatedAt()));
            return r;
        }).collect(Collectors.toList());

        Page<QuanTieListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(collect);
        return result;
    }

    public QuanTieBaseInfoResp getTieBaseInfo(Long tieId) {
        QuanBarTie e = quanBarTieService.getById(tieId);
        if (e == null) {
            return null;
        }
        Account account = accountService.selectById(e.getCreatedBy());
        QuanTieBaseInfoResp r = new QuanTieBaseInfoResp();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        r.setAvatar(e.getAvatar());
        r.setContent(e.getContent());
        if (account != null) {
            r.setCreatedName(account.getNickname());
        }
        r.setCreatedTime(DateUtils.date2Str(e.getCreatedAt()));
        return r;
    }

    public Boolean comment(QuanTieCommentReq req, Long userId, String userName) {
        Long commentId = quanTieCommentService.comment(req.getTieId(), userId, userName, req.getContent(), req.getReplyTo(), req.getFirstLevelCommonId());
        if (commentId != null) {
            //TODO yang 这里要处理下，提供一个公用的
            //messageFacade.createMessageOfComment(req.getTieId(), commentId, req.getReplyTo(), userId);
            return true;
        } else {
            return false;
        }
    }

    public Page<QuanTieCommentResp> commentShow(QuanTieCommentPageReq req, Long currentUserId) {
        Page<QuanTieComment> firstLevelCommentPage = quanTieCommentService.getFirstLevelCommentPageOfBar(Page.of(req.getPage(), req.getSize()), req);
        if (firstLevelCommentPage .getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<QuanTieCommentLike> likes = quanTieCommentLikeService.selectByTieId(req.getTieId());

        List<Long> userIds = firstLevelCommentPage.getRecords().stream().map(v -> v.getUserId()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        // 1. 映射全部评论
        Map<Long, List<QuanTieCommentLike>> commentId2LikeListMap = likes.stream().collect(Collectors.groupingBy(QuanTieCommentLike::getCommentId));

        // 组装评论树
        List<QuanTieCommentResp> result = new ArrayList<>();

        for (QuanTieComment record : firstLevelCommentPage.getRecords()) {
            QuanTieCommentResp r = convertToVO(record, currentUserId, commentId2LikeListMap, userId2UserInfoMap);
            if (req.getWithPreviewReplies() != null && req.getWithPreviewReplies()) {
                int previewCount = req.getPreviewReplyCount() != null ? req.getPreviewReplyCount() : 2;

                // 批量获取所有一级评论的预览回复
                List<Long> commentIds = firstLevelCommentPage.getRecords().stream().map(comment -> comment.getId()).collect(Collectors.toList());
                Map<Long, List<QuanTieComment>> previewRepliesMap = quanTieCommentService
                        .getPreviewRepliesForComments(commentIds, previewCount);

                // 获取回复相关的用户信息
                List<Long> replyUserIds = previewRepliesMap.values().stream()
                        .flatMap(List::stream)
                        .map(QuanTieComment::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
                Map<Long, Account> replyUserMap = accountService.selectByIds(replyUserIds).stream()
                        .collect(Collectors.toMap(Account::getId, v -> v));

                // 转换回复为VO并设置到主评论
                List<QuanTieComment> previewReplies = previewRepliesMap.getOrDefault(r.getId(), Collections.emptyList());
                List<QuanTieCommentResp> replyVOs = previewReplies.stream()
                        .map(s -> convertToVO(s, currentUserId, commentId2LikeListMap, replyUserMap))
                        .collect(Collectors.toList());
                r.setReplies(replyVOs);
                r.setReplyCount(quanTieCommentService.getReplyCount(r.getId()));
            }
            result.add(r);
        }

        Page<QuanTieCommentResp> pageResult = Page.of(req.getPage(), req.getSize());
        pageResult.setTotal(firstLevelCommentPage.getTotal());
        pageResult.setRecords(result);
        return pageResult;
    }


    private QuanTieCommentResp convertToVO(QuanTieComment comment, Long currentUserId, Map<Long, List<QuanTieCommentLike>> commentId2LikeListMap, Map<Long, Account> userId2UserInfoMap) {
        QuanTieCommentResp vo = new QuanTieCommentResp();
        vo.setId(comment.getId());
        vo.setTieId(comment.getTieId());
        vo.setContent(comment.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) ? "本条评论已被用户删除～" : comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyTo(comment.getReplyTo());
        vo.setReplyToName(comment.getReplyToUsername());
        vo.setDeleted(comment.getIsDeleted() != 0);
        vo.setLikes(commentId2LikeListMap.containsKey(comment.getId()) ? commentId2LikeListMap.get(comment.getId()).size() : 0);
        vo.setIsMine(comment.getUserId().equals(currentUserId));
        vo.setFirstLevelCommonId(comment.getFirstLevelCommonId());
        vo.setSecrecyId(userId2UserInfoMap.get(comment.getUserId()).getSecrecyId());

        // 获取用户昵称头像
        Account account = accountService.selectById(comment.getUserId());
        vo.setUsername(account.getNickname());
        vo.setAvatar(account.getAvatarUrl());
        return vo;
    }
}
