package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectCommentLike;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 17:43
 */
public interface ProjectCommentLikeService extends IService<ProjectCommentLike> {
    ProjectCommentLike selectByCommentIdAndUserId(Long commentId,Long projectId, Long userId);

    Boolean insert(Long commentId,Long projectId, Long userId);

    List<ProjectCommentLike> selectByProjectId(Long projectId);
}
