package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectApplications;
import com.example.mapper.ProjectApplicationsMapper;
import com.example.service.ProjectApplicationsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:37
 */
@Service
public class ProjectApplicationsServiceImpl extends ServiceImpl<ProjectApplicationsMapper, ProjectApplications> implements ProjectApplicationsService {
    @Override
    public ProjectApplications selectByProjectIdAndUserId(Long projectId, Long userId) {
        return this.baseMapper.selectOne(new QueryWrapper<ProjectApplications>().eq("project_id", projectId)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ProjectApplications> selectByProcessedBy(Long userId, Integer status) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectApplications>()
                .eq("processed_by", userId)
                .eq("status", status)
                .eq("is_deleted", 0));
    }

    @Override
    public List<ProjectApplications> selectByUserId(Long userId, Integer status) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectApplications>()
                .eq("user_id", userId)
                .eq("status", status)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<ProjectApplications> myApplyList(Page<ProjectApplications> page, Long userId, Integer status) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<ProjectApplications>()
                        .eq("status", status)
                        .eq("processed_by", userId)
                        .orderByDesc("apply_time")
        );
    }

    @Override
    public Integer updateStatus(Long id, Integer status, Long userId) {
        ProjectApplications p = new ProjectApplications();
        p.setStatus(status);
        p.setProcessedAt(new Date());
        p.setModifiedAt(new Date());
        p.setModifiedBy(userId);
        return this.baseMapper.update(p, new QueryWrapper<ProjectApplications>().eq("id", id));
    }
}
