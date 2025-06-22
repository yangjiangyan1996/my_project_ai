package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectCommentLike;
import com.example.mapper.ProjectCommentLikeMapper;
import com.example.service.ProjectCommentLikeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 17:43
 */
@Service
public class ProjectCommentLikeServiceImpl extends ServiceImpl<ProjectCommentLikeMapper, ProjectCommentLike> implements ProjectCommentLikeService {

    @Override
    public ProjectCommentLike selectByCommentIdAndUserId(Long commentId, Long projectId, Long userId) {
        return this.baseMapper.selectOne(new QueryWrapper<ProjectCommentLike>()
                .eq("comment_id", commentId)
                .eq("project_id", projectId)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public Boolean insert(Long commentId,Long projectId, Long userId) {
        return save(new ProjectCommentLike(null, commentId, userId,projectId));
    }

    @Override
    public List<ProjectCommentLike> selectByProjectId(Long projectId) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectCommentLike>()
                .eq("project_id", projectId)
                .eq("is_deleted", 0));
    }
}