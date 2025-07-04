package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.ProjectFavorite;
import com.example.entity.dto.ProjectLike;
import com.example.entity.dto.Projects;
import com.example.entity.dto.UserFollow;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.MyFollowCountResp;
import com.example.entity.resp.MyPublishedResp;
import com.example.enums.ProjectEnum;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:47
 */
@Service
public class MyFacade {
    @Resource
    UserFollowService userFollowService;
    @Resource
    ProjectCommentService projectCommentService;
    @Resource
    ProjectLikeService projectLikeService;
    @Resource
    ProjectFavoriteService projectFavoriteService;
    @Resource
    ProjectService projectService;

    public Page<MyPublishedResp> myPublished(MyPublishedPageReq req, Long userId) {
        Page<Projects> myProjects = projectService.getMyProjects(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (myProjects.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }
        List<Long> projectIds = myProjects.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());

        Map<Long, Long> projectId2LikeCountMap = projectLikeService.selectLikeCountByProjectIds(projectIds);
        Map<Long, Long> projectId2FavoriteCountMap = projectFavoriteService.selectFavoriteCountByProjectIds(projectIds);
        Map<Long, Long> projectId2CommentCountMap = projectCommentService.selectCommentCountByProjectIds(projectIds);

        List<MyPublishedResp> collect = myProjects.getRecords().stream().map(v -> {
            MyPublishedResp projectsResp = new MyPublishedResp();
            BeanUtils.copyProperties(v, projectsResp);
            ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
            projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
            projectsResp.setLikeCount(projectId2LikeCountMap.get(v.getId()));
            projectsResp.setCommentCount(projectId2CommentCountMap.get(v.getId()));
            projectsResp.setFavoriteCount(projectId2FavoriteCountMap.get(v.getId()));
            return projectsResp;
        }).collect(Collectors.toList());

        Page<MyPublishedResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(myProjects.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<MyPublishedResp> myFavorites(MyPublishedPageReq req, Long userId) {
        Page<ProjectFavorite> myFavorites = projectFavoriteService.getMyProjects(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (myFavorites.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> projectIds = myFavorites.getRecords().stream().map(v -> v.getProjectId()).collect(Collectors.toList());

        List<Projects> projectList = projectService.selectByProjectIds(projectIds);
        Map<Long, Long> projectId2LikeCountMap = projectLikeService.selectLikeCountByProjectIds(projectIds);
        Map<Long, Long> projectId2FavoriteCountMap = projectFavoriteService.selectFavoriteCountByProjectIds(projectIds);
        Map<Long, Long> projectId2CommentCountMap = projectCommentService.selectCommentCountByProjectIds(projectIds);


        List<MyPublishedResp> collect = projectList.stream().map(v -> {
            MyPublishedResp projectsResp = new MyPublishedResp();
            BeanUtils.copyProperties(v, projectsResp);
            ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
            projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
            projectsResp.setLikeCount(projectId2LikeCountMap.get(v.getId()));
            projectsResp.setCommentCount(projectId2CommentCountMap.get(v.getId()));
            projectsResp.setFavoriteCount(projectId2FavoriteCountMap.get(v.getId()));
            return projectsResp;
        }).collect(Collectors.toList());

        Page<MyPublishedResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(myFavorites.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<MyPublishedResp> myLike(MyPublishedPageReq req, Long userId) {
        Page<ProjectLike> myLikes = projectLikeService.getMyProjects(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (myLikes.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> projectIds = myLikes.getRecords().stream().map(v -> v.getProjectId()).collect(Collectors.toList());
        List<Projects> projectList = projectService.selectByProjectIds(projectIds);
        Map<Long, Long> projectId2LikeCountMap = projectLikeService.selectLikeCountByProjectIds(projectIds);
        Map<Long, Long> projectId2FavoriteCountMap = projectFavoriteService.selectFavoriteCountByProjectIds(projectIds);
        Map<Long, Long> projectId2CommentCountMap = projectCommentService.selectCommentCountByProjectIds(projectIds);

        List<MyPublishedResp> collect = projectList.stream().map(v -> {
            MyPublishedResp projectsResp = new MyPublishedResp();
            BeanUtils.copyProperties(v, projectsResp);
            ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
            projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
            projectsResp.setLikeCount(projectId2LikeCountMap.get(v.getId()));
            projectsResp.setCommentCount(projectId2CommentCountMap.get(v.getId()));
            projectsResp.setFavoriteCount(projectId2FavoriteCountMap.get(v.getId()));
            return projectsResp;
        }).collect(Collectors.toList());

        Page<MyPublishedResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(myLikes.getTotal());
        result.setRecords(collect);
        return result;
    }

    public MyFollowCountResp myFollowCount(Long userId) {
        List<UserFollow> myFollowers = userFollowService.selectByFollowerId(userId);
        List<UserFollow> myFollowees = userFollowService.selectByFolloweeId(userId);
        return new MyFollowCountResp(myFollowers.size(), myFollowees.size());
    }
}
