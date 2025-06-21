package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.dto.ProjectsDetail;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.service.*;
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
    ProjectFavoriteService projectFavoriteService;
    @Resource
    ProjectLikeService projectLikeService;
    @Resource
    ProjectsDetailService projectsDetailService;
    @Resource
    private ProjectService projectService;
    @Resource
    AccountService accountService;
    public ProjectsDetailResp selectByProjectId(Long projectId, Long userId) {
        boolean projectDown = projectService.isProjectDown(projectId);
        if (!projectDown) {
            throw new ValidationException("项目已下架");
        }
        ProjectsDetailResp r = new ProjectsDetailResp();
        ProjectsDetail project = projectsDetailService.selectByProjectId(projectId);
        BeanUtils.copyProperties(project, r);

        Account account = accountService.selectById(project.getCreatedBy());
        r.setCreatorName(account.getNickname());

        Boolean myLike = projectLikeService.selectByProjectIdAndUserId(projectId, userId);
        r.setMyLike(myLike);
        Boolean myFavorite = projectFavoriteService.selectByProjectIdAndUserId(projectId, userId);
        r.setMyFavorite(myFavorite);

        return r;
    }

    public Boolean likeProject(Long projectId, Long id, Boolean liked) {
        return projectLikeService.likeProject(projectId, id, liked);
    }

    public Boolean favoriteProject(Long projectId, Long id, Boolean liked) {
        return projectFavoriteService.favoriteProject(projectId, id, liked);
    }
}
