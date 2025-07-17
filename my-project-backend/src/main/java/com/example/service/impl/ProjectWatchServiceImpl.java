package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectWatch;
import com.example.mapper.ProjectWatchMapper;
import com.example.service.ProjectWatchService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/16 20:27
 */
@Service
public class ProjectWatchServiceImpl extends ServiceImpl<ProjectWatchMapper, ProjectWatch> implements ProjectWatchService {
    @Override
    public Boolean insertOne(Long projectId, Long userId) {
        ProjectWatch e = new ProjectWatch();
        e.setProjectId(projectId);
        e.setUserId(userId);
        e.setCreatedBy(userId);
        e.setCreatedAt(new Date());
        e.setModifiedBy(userId);
        e.setModifiedAt(new Date());
        return this.baseMapper.insert(e) > 0;
    }

    @Override
    public ProjectWatch selectByProjectIdAndUserId(Long projectId, Long userId) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<ProjectWatch>()
                .eq(ProjectWatch::getProjectId, projectId)
                .eq(ProjectWatch::getUserId, userId)
                .eq(ProjectWatch::getIsDeleted, 0));
    }
}
