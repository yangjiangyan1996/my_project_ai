package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectReview;
import com.example.mapper.ProjectReviewMapper;
import com.example.service.ProjectReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 10:11
 */
@Service
@Slf4j
public class ProjectReviewServiceImpl extends ServiceImpl<ProjectReviewMapper, ProjectReview> implements ProjectReviewService {
    @Override
    public List<ProjectReview> selectByProjectId(Long projectId) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectReview>()
                .eq("project_id", projectId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ProjectReview> selectByProjectIdAndFromUserId(Long projectId, Long userId) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectReview>()
                .eq("project_id", projectId)
                .eq("from_user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public ProjectReview selectByProjectIdAndFromUserIdAndToUserId(Long projectId, Long fromUserId, Long toUserId) {
        return this.baseMapper.selectOne(new QueryWrapper<ProjectReview>()
                .eq("project_id", projectId)
                .eq("from_user_id", fromUserId)
                .eq("to_user_id", toUserId)
                .eq("is_deleted", 0));
    }
}
