package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectComment;

import java.util.List;
import java.util.Map;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 16:37
 */
public interface ProjectCommentService extends IService<ProjectComment> {
    Boolean comment(Long projectId, Long userId, String userName, String content, Long replyToId, Long firstLevelCommonId);

    List<ProjectComment> selectByProjectId(Long projectId);

    ProjectComment selectByProjectAndCommentId(Long projectId, Long commentId);

    Integer updateStatus(Long commentId, Long userId, Integer code);

    Map<Long, Long> selectCommentCountByProjectIds(List<Long> projectIds);

    ProjectComment selectById(Long id);
}
