package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectsDetail;
import com.example.entity.resp.ProjectsDetailResp;

public interface ProjectsDetailService extends IService<ProjectsDetail> {
    ProjectsDetail selectByProjectId(Long projectId);
}