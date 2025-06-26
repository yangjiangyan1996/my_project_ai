package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectApplications;
import com.example.mapper.ProjectApplicationsMapper;
import com.example.service.ProjectApplicationsService;
import org.springframework.stereotype.Service;

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
                .eq("is_deleted",0));
    }
}
