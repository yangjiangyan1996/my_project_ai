package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectFavorite;

import java.util.List;
import java.util.Map;

/**
 * 收藏
 */
public interface ProjectFavoriteService extends IService<ProjectFavorite> {
    Boolean favoriteProject(Long projectId, Long userId, Boolean favorited);

    Boolean selectByProjectIdAndUserId(Long projectId, Long userId);

    Long selectCountByProjectId(Long projectId);

    Page<ProjectFavorite> getMyProjects(Page<ProjectFavorite> of, Long userId);

    Map<Long, Long> selectFavoriteCountByProjectIds(List<Long> projectIds);

    Map<Long, Integer> selectProjectId2FavoriteCount();
}