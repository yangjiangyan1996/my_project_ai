package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:54
 */
public interface ProjectService extends IService<Projects> {
    Page getHotFuyeProjects(Page<Projects> pageable, ProjectListReq req);

    //判断项目是否启用
    boolean isProjectDown(Long projectId);
}
