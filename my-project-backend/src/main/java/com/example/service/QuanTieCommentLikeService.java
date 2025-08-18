package com.example.service;

import com.example.entity.dto.QuanTieCommentLike;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:27
 */
public interface QuanTieCommentLikeService {
    List<QuanTieCommentLike> selectByTieId(Long tieId);

    QuanTieCommentLike selectByCommentIdAndUserId(Long commentId, Long tieId, Long userId);

    Boolean insert(Long commentId, Long tieId, Long userId);

    List<QuanTieCommentLike> selectByTieIds(List<Long> tieIds);
}
