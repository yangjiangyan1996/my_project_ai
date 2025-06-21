package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectFavorite;
import com.example.entity.dto.ProjectLike;
import com.example.mapper.ProjectFavoriteMapper;
import com.example.service.ProjectFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class ProjectFavoriteServiceImpl extends ServiceImpl<ProjectFavoriteMapper, ProjectFavorite> implements ProjectFavoriteService {
    @Override
    public Boolean favoriteProject(Long projectId, Long userId, Boolean favorited) {
        boolean exists = selectByProjectIdAndUserId(projectId, userId);
        if (Boolean.TRUE.equals(favorited)) {
            //先判断数据库是否存在数据，有的话直接返回true
            if (exists) {
                return true;
            }
            return save(new ProjectFavorite(null, userId, projectId));
        } else {
            if (!exists) {
                return true;
            }
            return remove(new UpdateWrapper<ProjectFavorite>()
                    .eq("project_id", projectId)
                    .eq("user_id", userId)
                    .set("is_deleted", 1));
        }
    }

    @Override
    public Boolean selectByProjectIdAndUserId(Long projectId, Long userId) {
        return this.baseMapper.exists(new LambdaQueryWrapper<ProjectFavorite>()
                .eq(ProjectFavorite::getProjectId, projectId)
                .eq(ProjectFavorite::getUserId, userId)
                .eq(ProjectFavorite::getIsDeleted, 0));
    }

    @Override
    public Long selectCountByProjectId(Long projectId) {
        return this.baseMapper.selectCount(new LambdaQueryWrapper<ProjectFavorite>()
                .eq(ProjectFavorite::getProjectId, projectId)
                .eq(ProjectFavorite::getIsDeleted, 0));
    }
}