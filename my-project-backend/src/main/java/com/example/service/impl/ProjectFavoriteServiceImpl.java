package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectFavorite;
import com.example.entity.dto.ProjectLike;
import com.example.entity.dto.Projects;
import com.example.mapper.ProjectFavoriteMapper;
import com.example.service.ProjectFavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Page<ProjectFavorite> getMyProjects(Page<ProjectFavorite> pageable, Long userId) {
        return this.baseMapper.selectPage(
                pageable,
                new QueryWrapper<ProjectFavorite>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
        );
    }

    @Override
    public Map<Long, Long> selectFavoriteCountByProjectIds(List<Long> projectIds) {
        List<ProjectFavorite> projectLikes = this.baseMapper.selectList(
                new QueryWrapper<ProjectFavorite>()
                        .in("project_id", projectIds)
                        .eq("is_deleted",0)
        );
        //根据projectId分类，获取map,key是projectId, value是数量
        return projectLikes.stream().collect(Collectors.groupingBy(ProjectFavorite::getProjectId, Collectors.counting()));
    }

    @Override
    public Map<Long, Integer> selectProjectId2FavoriteCount() {
        //统计出projectID对应的user_id的人数
        List<ProjectFavorite> projectFavorites = this.baseMapper.selectList(
                new QueryWrapper<ProjectFavorite>()
                        .select("project_id, count(user_id) as favoriteCount")
                        .groupBy("project_id")
        );
        return projectFavorites.stream().collect(Collectors.toMap(ProjectFavorite::getProjectId, ProjectFavorite::getFavoriteCount));
    }
}