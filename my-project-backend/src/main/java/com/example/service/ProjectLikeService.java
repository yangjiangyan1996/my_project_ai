package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectLike;

import java.util.List;
import java.util.Map;

/**
 * @Author YangJian
 * @Description 点赞
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 17:55
 */
public interface ProjectLikeService extends IService<ProjectLike> {
    Boolean likeProject(Long projectId, Long userId, Boolean liked);

    Boolean selectByProjectIdAndUserId(Long projectId, Long userId);

    Long selectCountByProjectId(Long projectId);

    Page<ProjectLike> getMyProjects(Page<ProjectLike> of, Long userId);

    Map<Long, Long> selectLikeCountByProjectIds(List<Long> projectIds);

    Map<Long, Integer> selectProjectId2LikeCount();
}
