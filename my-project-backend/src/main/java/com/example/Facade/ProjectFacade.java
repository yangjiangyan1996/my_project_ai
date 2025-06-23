package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.dto.ProjectComment;
import com.example.entity.dto.ProjectCommentLike;
import com.example.entity.dto.ProjectsDetail;
import com.example.entity.req.UserCommentProjectReq;
import com.example.entity.resp.ProjectCommentResp;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.enums.ProjectEnum;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 17:56
 */
@Service
public class ProjectFacade {

    @Resource
    ProjectCommentLikeService projectCommentLikeService;
    @Resource
    ProjectCommentService projectCommentService;
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

        r.setLikeCount(projectLikeService.selectCountByProjectId(projectId));
        r.setFavoriteCount(projectFavoriteService.selectCountByProjectId(projectId));
        return r;
    }

    public Boolean likeProject(Long projectId, Long id, Boolean liked) {
        return projectLikeService.likeProject(projectId, id, liked);
    }

    public Boolean favoriteProject(Long projectId, Long id, Boolean liked) {
        return projectFavoriteService.favoriteProject(projectId, id, liked);
    }

    public Boolean comment(UserCommentProjectReq req, Long userId, String name) {
        return projectCommentService.comment(req.getProjectId(), userId, name, req.getContent(), req.getReplyTo(),req.getFirstLevelCommonId());
    }

    public List<ProjectCommentResp> commentShow(Long projectId, Long currentUserId) {
        List<ProjectComment> all = projectCommentService.selectByProjectId(projectId)
                .stream()
                .sorted(Comparator.comparing(ProjectComment::getCreatedAt))
                .collect(Collectors.toList());
        List<ProjectCommentLike> likes = projectCommentLikeService.selectByProjectId(projectId);

        Map<Long, ProjectCommentResp> idMap = new HashMap<>();
        List<ProjectCommentResp> roots = new ArrayList<>();

        // 1. 映射全部评论
        Map<Long, List<ProjectCommentLike>> commentId2LikeListMap = likes.stream().collect(Collectors.groupingBy(ProjectCommentLike::getCommentId));

        // 构建第一层评论（replyTo=0）
        Map<Long, ProjectCommentResp> firstLevelMap = all.stream()
                .filter(c -> c.getReplyTo() < 0)
                .sorted(Comparator.comparing(ProjectComment::getCreatedAt))
                .collect(Collectors.toMap(
                        ProjectComment::getId,
                        c -> convertToVO(c, currentUserId, commentId2LikeListMap)
                ));

        // 构建第二层评论（first_level_common_id对应第一层ID）
        Map<Long, List<ProjectCommentResp>> secondLevelMap = all.stream()
                .filter(c -> c.getReplyTo() > 0)
                .sorted(Comparator.comparing(ProjectComment::getCreatedAt))
                .collect(Collectors.groupingBy(
                        ProjectComment::getFirstLevelCommonId,
                        Collectors.mapping(
                                c -> convertToVO(c, currentUserId, commentId2LikeListMap),
                                Collectors.toList()
                        )
                ));

        // 组装评论树
        secondLevelMap.forEach((firstLevelId, replies) -> {
            ProjectCommentResp parent = firstLevelMap.get(firstLevelId);
            if (parent != null) {
                parent.setReplies(replies);
            }
        });

        return new ArrayList<>(firstLevelMap.values());
    }

    private ProjectCommentResp convertToVO(ProjectComment comment, Long currentUserId,Map<Long, List<ProjectCommentLike>> commentId2LikeListMap) {
        ProjectCommentResp vo = new ProjectCommentResp();
        vo.setId(comment.getId());
        vo.setProjectId(comment.getProjectId());
        vo.setContent(comment.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) ? "本条评论已被用户删除～" : comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyTo(comment.getReplyTo());
        vo.setDeleted(comment.getIsDeleted() != 0);
        vo.setLikes(commentId2LikeListMap.containsKey(comment.getId()) ?commentId2LikeListMap.get(comment.getId()).size():0);
        vo.setIsMine(comment.getUserId().equals(currentUserId));
        vo.setFirstLevelCommonId(comment.getFirstLevelCommonId());

        // 获取用户昵称头像
        Account account = accountService.selectById(comment.getUserId());
        vo.setUsername(account.getNickname());
        vo.setAvatar(account.getAvatarUrl());
        return vo;
    }

    public Boolean commentDeleted(Long projectId, Long commentId, Long userId) {
        //判断评论是不是自己的，如果不是，返回"不是自己评论的，无法删除！"
        ProjectComment pc = projectCommentService.selectByProjectAndCommentId(projectId, commentId);
        if (pc == null) {
            throw new ValidationException("没有此评论！");
        }
        if (!pc.getUserId().equals(userId)) {
            throw new ValidationException("不是自己评论的，无法删除！");
        }
        if (pc.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode())) {
            throw new ValidationException("已删除，无需重复操作！");
        }
        //删除评论
        return projectCommentService.updateStatus(commentId, userId, ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) == 1;
    }

    public Boolean commentLike(Long commentId,Long projectId, Long userId) {
        ProjectCommentLike l = projectCommentLikeService.selectByCommentIdAndUserId(commentId,projectId, userId);
        if (l != null) {
            throw  new ValidationException("已点赞，无需重复操作！");
        }
        return projectCommentLikeService.insert(commentId, projectId,userId);
    }
}
