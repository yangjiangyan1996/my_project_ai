package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.QuanTieComment;
import com.example.entity.req.QuanTieCommentPageReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:28
 */
public interface QuanTieCommentService {
    Long comment(Long tieId, Long userId, String userName, String content, Long replyTo, Long firstLevelCommonId);

    Page<QuanTieComment> getFirstLevelCommentPageOfBar(Page<QuanTieComment> of, QuanTieCommentPageReq req);
}
