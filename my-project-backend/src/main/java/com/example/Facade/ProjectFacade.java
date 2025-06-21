package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.Account;
import com.example.entity.dto.ProjectsDetail;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.service.AccountService;
import com.example.service.ProjectService;
import com.example.service.ProjectsDetailService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 17:56
 */
@Service
public class ProjectFacade {

    @Resource
    ProjectsDetailService projectsDetailService;
    @Resource
    private ProjectService projectService;
    @Resource
    AccountService accountService;
    public ProjectsDetailResp selectByProjectId(Long projectId) {
        boolean projectDown = projectService.isProjectDown(projectId);
        if (!projectDown) {
            throw new ValidationException("项目已下架");
        }
        ProjectsDetailResp r = new ProjectsDetailResp();
        ProjectsDetail project = projectsDetailService.selectByProjectId(projectId);
        BeanUtils.copyProperties(project, r);

        Account account = accountService.selectById(project.getCreatedBy());
        r.setCreatorName(account.getNickname());


        return r;
    }
}
