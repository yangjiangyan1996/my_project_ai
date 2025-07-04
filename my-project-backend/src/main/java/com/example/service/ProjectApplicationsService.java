package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Images;
import com.example.entity.dto.ProjectApplications;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:37
 */
public interface ProjectApplicationsService extends IService<ProjectApplications> {
    ProjectApplications selectByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectApplications> selectByProcessedBy(Long userId, Integer  status);

    List<ProjectApplications> selectByUserId(Long userId, Integer  status);

    Page<ProjectApplications> myApplyList(Page<ProjectApplications> page, Long userId, Integer status);

    Page<ProjectApplications> myApplicationList(Page<ProjectApplications> page, Long userId);

    Integer updateStatus(Long id, Integer status, Long userId);

    int updateStatusByProjectId(Integer status, Long projectId, Integer butNotThisStatus);
}

