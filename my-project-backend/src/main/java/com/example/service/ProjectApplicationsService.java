package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Images;
import com.example.entity.dto.ProjectApplications;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:37
 */
public interface ProjectApplicationsService extends IService<ProjectApplications> {
    ProjectApplications selectByProjectIdAndUserId(Long projectId, Long userId);
}

