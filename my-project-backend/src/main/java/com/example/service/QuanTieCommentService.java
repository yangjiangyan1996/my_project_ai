package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.QuanTieComment;
import com.example.entity.req.QuanTieCommentPageReq;

import java.util.List;
import java.util.Map;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:28
 */
public interface QuanTieCommentService {
    Long comment(Long tieId, Long userId, String userName, String content, Long replyTo, Long firstLevelCommonId);

    Page<QuanTieComment> getFirstLevelCommentPageOfBar(Page<QuanTieComment> of, QuanTieCommentPageReq req);

    Map<Long, List<QuanTieComment>> getPreviewRepliesForComments(List<Long> commentIds, int previewCount);

    Long getReplyCount(Long commenId);
}
