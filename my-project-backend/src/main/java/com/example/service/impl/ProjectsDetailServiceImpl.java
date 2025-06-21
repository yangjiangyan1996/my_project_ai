package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.Projects;
import com.example.entity.dto.ProjectsDetail;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.mapper.ProjectsDetailMapper;
import com.example.mapper.ProjectsMapper;
import com.example.service.AccountService;
import com.example.service.ProjectService;
import com.example.service.ProjectsDetailService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ProjectsDetailServiceImpl extends ServiceImpl<ProjectsDetailMapper, ProjectsDetail> implements ProjectsDetailService {
    @Resource
    private ProjectsDetailMapper projectsDetailMapper;



    @Override
    public ProjectsDetail selectByProjectId(Long projectId) {
        ProjectsDetail project = projectsDetailMapper.selectOne(new QueryWrapper<ProjectsDetail>().eq("projects_id", projectId));
        return project;
    }

}