package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.enums.ProjectEnum;
import com.example.mapper.ProjectsMapper;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
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
    public Page<Projects> getHotFuyeProjects(Page<Projects> pageable, ProjectListReq req,List<Integer> firstLevel,List<Integer> secondLevel) {
        return projectMapper.selectPage(
                pageable,
                new QueryWrapper<Projects>()
                        .gt("status", ProjectEnum.ProjectStatusEnum.NO.getCode())
                        .in(!CollectionUtils.isEmpty(firstLevel), "first_category", firstLevel)
                        .in(!CollectionUtils.isEmpty(secondLevel), "second_category", secondLevel)
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
                        .eq(userId != null, "created_by", userId)
                        .orderByDesc("created_at")
        );
    }

    @Override
    public Page getProjectsPageByStatus(Page<Projects> pageable, Integer status) {
        return projectMapper.selectPage(
                pageable,
                new QueryWrapper<Projects>()
                        .eq(status != null, "status", status)
                        .orderByDesc("created_at")
        );
    }

    @Override
    public boolean isProjectDown(Long projectId) {
        Projects projects = projectMapper.selectOne(new QueryWrapper<Projects>().eq("id", projectId)
                .gt("status", ProjectEnum.ProjectStatusEnum.NO.getCode())
                .lt("status", ProjectEnum.ProjectStatusEnum.CLOSED.getCode()));
        return projects != null;
    }

    @Override
    public Projects selectByProjectId(Long projectId) {
        return projectMapper.selectOne(new QueryWrapper<Projects>().eq("id", projectId));
    }

    @Override
    public Projects selectByProjectIdAndStatus(Long projectId, Integer status) {
        return projectMapper.selectOne(new QueryWrapper<Projects>().eq("id", projectId)
                .eq(status!=null, "status", status));
    }

    @Override
    public List<Projects> selectByProjectIds(List<Long> projectIds) {
        return this.baseMapper.selectList(new QueryWrapper<Projects>().in("id", projectIds)
                .gt("status", ProjectEnum.ProjectStatusEnum.NO.getCode())
                .lt("status", ProjectEnum.ProjectStatusEnum.CLOSED.getCode()).eq("is_deleted", 0));
    }

    @Override
    public Boolean updateStatus(Long projectId, Integer status,String reason, Long userId) {
        Projects projects = new Projects();
        projects.setStatus(status);
        projects.setModifiedBy(userId);
        projects.setModifiedAt(new Date());
        if (StringUtils.isNotBlank(reason)) {
            projects.setReason(reason);
        }
        return this.baseMapper.update(projects, new QueryWrapper<Projects>().eq("id", projectId)) > 0;
    }
}
