package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.req.MyFolloweesPageReq;
import com.example.entity.req.MyFollowersPageReq;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.*;
import com.example.enums.CommonEnum;
import com.example.enums.MessageEnums;
import com.example.enums.ProjectEnum;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
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
    AccountService  accountService;
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

    public Page<MyFollowersPageResp> myFollowers(MyFollowersPageReq req, Long userId) {
        Page<UserFollow> list = userFollowService.selectPageByFolloweeId(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> userIds = list.getRecords().stream().map(v -> v.getFollowerId()).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<UserFollow> userFollows = userFollowService.selectByFollowerId(userId);
        List<Long> interrelationUserIds = userFollows.stream().filter(v -> userIds.contains(v.getFolloweeId()))
                .map(v -> v.getFolloweeId())
                .collect(Collectors.toList());


        List<MyFollowersPageResp> collect = list.getRecords().stream().map(v -> {
                    MyFollowersPageResp projectsResp = new MyFollowersPageResp();

                    if (!CollectionUtils.isEmpty(userId2UserInfoMap) && userId2UserInfoMap.containsKey(v.getFollowerId())) {
                        Account account = userId2UserInfoMap.get(v.getFollowerId());
                        projectsResp.setUserId(account.getId());
                        projectsResp.setUsername(account.getUsername());
                        projectsResp.setSecrecyId(account.getSecrecyId());
                        projectsResp.setAvatarUrl(account.getAvatarUrl());
                        projectsResp.setCreatedAt(v.getCreatedAt());
                        projectsResp.setIndustryName(CommonEnum.IndustryEnum.getByCode(account.getIndustryCode()));
                    }
                    projectsResp.setNeedFollow(interrelationUserIds.contains(v.getFollowerId()));
                    return projectsResp;
                })
                .sorted(Comparator.comparing(MyFollowersPageResp::getCreatedAt))
                .collect(Collectors.toList());

        Page<MyFollowersPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<MyFolloweesPageResp> myFollowees(MyFolloweesPageReq req, Long userId) {
        Page<UserFollow> list = userFollowService.selectPageByFollowerId(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> userIds = list.getRecords().stream().map(v -> v.getFolloweeId()).distinct().collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<UserFollow> userFollows = userFollowService.selectByFolloweeId(userId);
        List<Long> interrelationUserIds = userFollows.stream().filter(v -> userIds.contains(v.getFollowerId()))
                .map(v -> v.getFollowerId())
                .collect(Collectors.toList());


        List<MyFolloweesPageResp> collect = list.getRecords().stream().map(v -> {
                    MyFolloweesPageResp projectsResp = new MyFolloweesPageResp();

                    if (!CollectionUtils.isEmpty(userId2UserInfoMap) && userId2UserInfoMap.containsKey(v.getFolloweeId())) {
                        Account account = userId2UserInfoMap.get(v.getFolloweeId());
                        projectsResp.setUserId(account.getId());
                        projectsResp.setUsername(account.getUsername());
                        projectsResp.setSecrecyId(account.getSecrecyId());
                        projectsResp.setAvatarUrl(account.getAvatarUrl());
                        projectsResp.setCreatedAt(v.getCreatedAt());
                        projectsResp.setIndustryName(CommonEnum.IndustryEnum.getByCode(account.getIndustryCode()));
                    }
                    projectsResp.setNeedFollow(interrelationUserIds.contains(v.getFollowerId()));
                    return projectsResp;
                })
                .sorted(Comparator.comparing(MyFolloweesPageResp::getCreatedAt))
                .collect(Collectors.toList());

        Page<MyFolloweesPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }
}
