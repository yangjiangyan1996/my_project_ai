package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectsDetail;

public interface ProjectsDetailService extends IService<ProjectsDetail> {
    ProjectsDetail selectByProjectId(Long projectId);
}