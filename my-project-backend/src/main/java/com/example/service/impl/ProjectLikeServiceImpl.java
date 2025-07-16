package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectFavorite;
import com.example.entity.dto.ProjectLike;
import com.example.mapper.ProjectLikeMapper;
import com.example.service.ProjectLikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectLikeServiceImpl extends ServiceImpl<ProjectLikeMapper, ProjectLike> implements ProjectLikeService {
    @Override
    public Boolean likeProject(Long projectId, Long userId, Boolean liked) {
        boolean exists =selectByProjectIdAndUserId(projectId, userId);
        if (Boolean.TRUE.equals(liked)) {
            //先判断数据库是否存在数据，有的话直接返回true
            if (exists) {
                return true;
            }
            return save(new ProjectLike(null, userId, projectId));
        } else {
            if (!exists) {
                return true;
            }
            return remove(new UpdateWrapper<ProjectLike>()
                    .eq("project_id", projectId)
                    .eq("user_id", userId)
                    .set("is_deleted", 1));
        }
    }

    @Override
    public Boolean selectByProjectIdAndUserId(Long projectId, Long userId) {
        return  this.baseMapper.exists(new LambdaQueryWrapper<ProjectLike>()
                .eq(ProjectLike::getProjectId, projectId)
                .eq(userId != null, ProjectLike::getUserId, userId)
                .eq(ProjectLike::getIsDeleted, 0));
    }

    @Override
    public Long selectCountByProjectId(Long projectId) {
        return this.baseMapper.selectCount(new LambdaQueryWrapper<ProjectLike>()
                .eq(ProjectLike::getProjectId, projectId)
                .eq(ProjectLike::getIsDeleted, 0));
    }

    @Override
    public Page<ProjectLike> getMyProjects(Page<ProjectLike> pageable, Long userId) {
        return this.baseMapper.selectPage(
                pageable,
                new QueryWrapper<ProjectLike>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
        );
    }

    @Override
    public Map<Long, Long> selectLikeCountByProjectIds(List<Long> projectIds) {
        List<ProjectLike> projectLikes = this.baseMapper.selectList(
                new QueryWrapper<ProjectLike>()
                        .in("project_id", projectIds)
                        .eq("is_deleted",0)
        );
        //根据projectId分类，获取map,key是projectId, value是数量
        return projectLikes.stream().collect(Collectors.groupingBy(ProjectLike::getProjectId, Collectors.counting()));
    }

    @Override
    public Map<Long, Integer> selectProjectId2LikeCount() {
        //统计出projectID对应的user_id的人数
        List<ProjectLike> projectFavorites = this.baseMapper.selectList(
                new QueryWrapper<ProjectLike>()
                        .select("project_id, count(user_id) as count")
                        .groupBy("project_id")
        );
        return projectFavorites.stream().collect(Collectors.toMap(ProjectLike::getProjectId, ProjectLike::getCount));
    }
}