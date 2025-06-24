package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.mapper.ProjectsMapper;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:54
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectsMapper, Projects> implements ProjectService {

    @Resource
    private ProjectsMapper projectMapper;

    @Override
    public Page<Projects> getHotFuyeProjects(Page<Projects> pageable, ProjectListReq req) {
        return projectMapper.selectPage(
                pageable,
                new QueryWrapper<Projects>()
                        .select("id", "name", "category", "description", "difficulty", "image_url", "created_at")
                        .eq("status", 1)
                        .eq(req.getCategory() != null, "category", req.getCategory())
                        .in(!CollectionUtils.isEmpty(req.getDifficulty()), "difficulty", req.getDifficulty())
                        .like(StringUtils.isNotBlank(req.getProjectName()), "name", req.getProjectName())
                        .orderByDesc("created_at")
        );
    }

    @Override
    public Page getMyProjects(Page<Projects> pageable, Long userId) {
        return projectMapper.selectPage(
                pageable,
                new QueryWrapper<Projects>()
                        .eq("created_by", userId)
                        .orderByDesc("created_at")
        );
    }

    @Override
    public boolean isProjectDown(Long projectId) {
        Projects projects = projectMapper.selectOne(new QueryWrapper<Projects>().eq("id", projectId)
                .eq("status", 1));
        return projects != null;
    }

    @Override
    public Projects selectByProjectId(Long projectId) {
        return projectMapper.selectOne(new QueryWrapper<Projects>().eq("id", projectId)
                .eq("status", 1));
    }

    @Override
    public List<Projects> selectByProjectIds(List<Long> projectIds) {
        return this.baseMapper.selectList(new QueryWrapper<Projects>().in("id", projectIds).eq("status", 1).eq("is_deleted", 0));
    }
}
