package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.entity.req.ShowHotProjectListPageReq;
import com.example.entity.resp.ShowHotProjectListPageResp;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:54
 */
public interface ProjectService extends IService<Projects> {
    Page getHotFuyeProjects(Page<Projects> pageable, ProjectListReq req,List<Integer> firstLevel,List<Integer> secondLevel);

    Page getMyProjects(Page<Projects> pageable, Long userId);

    Page getProjectsPageByStatus(Page<Projects> pageable, Integer status);

    //判断项目是否启用
    boolean isProjectDown(Long projectId);

    Projects selectByProjectId(Long projectId);

    Projects selectByProjectIdAndStatus(Long projectId, Integer status);

    List<Projects> selectByProjectIds(List<Long> projectIds);

    Boolean updateStatus(Long projectId, Integer status, String reason, Long userId);

}
