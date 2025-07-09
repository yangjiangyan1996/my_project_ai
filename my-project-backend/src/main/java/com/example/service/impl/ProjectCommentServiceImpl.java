package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectComment;
import com.example.entity.dto.ProjectFavorite;
import com.example.mapper.ProjectCommentMapper;
import com.example.service.ProjectCommentService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/21 16:37
 */
@Service
public class ProjectCommentServiceImpl extends ServiceImpl<ProjectCommentMapper, ProjectComment> implements ProjectCommentService {
    @Override
    public Boolean comment(Long projectId, Long userId, String userName, String content, Long replyToId,Long firstLevelCommonId) {
        ProjectComment replyToComment = null;
        if (replyToId != null) {
            replyToComment = this.baseMapper.selectById(replyToId);
        }
        ProjectComment projectComment = new ProjectComment();
        projectComment.setProjectId(projectId);
        projectComment.setUserId(userId);
        projectComment.setUsername(userName);
        projectComment.setContent(content);
        projectComment.setReplyTo(replyToId);
        projectComment.setFirstLevelCommonId(firstLevelCommonId);
        projectComment.setReplyToUserId(replyToComment != null ? replyToComment.getUserId() : null);
        projectComment.setReplyToUsername(replyToComment != null ? replyToComment.getUsername() : null);
        projectComment.setLikes(0);
        return save(projectComment);
    }

    @Override
    public List<ProjectComment> selectByProjectId(Long projectId) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectComment>().eq("project_id", projectId)
                .eq("is_deleted", 0));
    }


    @Override
    public ProjectComment selectByProjectAndCommentId(Long projectId, Long commentId) {
        return this.baseMapper.selectOne(new QueryWrapper<ProjectComment>()
                .eq("project_id", projectId)
                .eq("id", commentId)
                .eq("is_deleted", 0));
    }

    @Override
    public Integer updateStatus(Long commentId, Long userId, Integer code) {
        ProjectComment c = new ProjectComment();
        c.setId(commentId);
        c.setStatus(code);
        c.setModifiedAt(new Date());
        c.setModifiedBy(userId);
        return this.baseMapper.update(c, new QueryWrapper<ProjectComment>().eq("id", commentId));
    }

    @Override
    public Map<Long, Long> selectCommentCountByProjectIds(List<Long> projectIds) {
        List<ProjectComment> projectLikes = this.baseMapper.selectList(
                new QueryWrapper<ProjectComment>()
                        .in("project_id", projectIds)
                        .eq("is_deleted",0)
        );
        //根据projectId分类，获取map,key是projectId, value是数量
        return projectLikes.stream().collect(Collectors.groupingBy(ProjectComment::getProjectId, Collectors.counting()));

    }

    @Override
    public ProjectComment selectById(Long id) {
        return this.baseMapper.selectById(id);
    }
}
