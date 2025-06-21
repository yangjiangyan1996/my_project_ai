package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectLike;
import com.example.mapper.ProjectLikeMapper;
import com.example.service.ProjectLikeService;
import org.springframework.stereotype.Service;

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
                .eq(ProjectLike::getUserId, userId)
                .eq(ProjectLike::getIsDeleted, 0));
    }
}