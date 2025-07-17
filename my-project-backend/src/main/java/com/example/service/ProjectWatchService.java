package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectWatch;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/16 20:27
 */
public interface ProjectWatchService  extends IService<ProjectWatch> {
    Boolean insertOne(Long projectId, Long userId);

    ProjectWatch selectByProjectIdAndUserId(Long projectId, Long userId);
}
