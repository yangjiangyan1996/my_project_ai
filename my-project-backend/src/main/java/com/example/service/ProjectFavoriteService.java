package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectFavorite;

/**
 * 收藏
 */
public interface ProjectFavoriteService extends IService<ProjectFavorite> {
    Boolean favoriteProject(Long projectId, Long userId, Boolean favorited);

    Boolean selectByProjectIdAndUserId(Long projectId, Long userId);

    Long selectCountByProjectId(Long projectId);
}