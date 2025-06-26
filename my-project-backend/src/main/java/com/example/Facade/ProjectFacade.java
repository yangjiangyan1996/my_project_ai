package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.req.*;
import com.example.entity.resp.ProjectCommentResp;
import com.example.entity.resp.ProjectOfMyShowGetResp;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.enums.ProjectEnum;
import com.example.enums.UserEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    ProjectApplicationsService projectApplicationsService;
    @Resource
    ProjectMembersService projectMembersService;
    @Resource
    AccountShowService accountShowService;
    @Resource
    UserFollowService userFollowService;
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
        Projects projectDown = projectService.selectByProjectId(projectId);
        if (projectDown == null) {
            return new ProjectsDetailResp();
        }

        ProjectsDetailResp r = new ProjectsDetailResp();
        ProjectsDetail project = projectsDetailService.selectByProjectId(projectId);
        BeanUtils.copyProperties(project, r);

        r.setImageUrl(projectDown.getImageUrl());
        Account account = accountService.selectById(project.getCreatedBy());
        r.setCreatorName(account.getNickname());

        Boolean myLike = projectLikeService.selectByProjectIdAndUserId(projectId, userId);
        r.setMyLike(myLike);
        Boolean myFavorite = projectFavoriteService.selectByProjectIdAndUserId(projectId, userId);
        r.setMyFavorite(myFavorite);

        r.setLikeCount(projectLikeService.selectCountByProjectId(projectId));
        r.setFavoriteCount(projectFavoriteService.selectCountByProjectId(projectId));

        Boolean followed = userFollowService.selectByUserIdAndFollowedId(userId, project.getCreatedBy());
        r.setFollowed(followed);

        ProjectApplications pa = projectApplicationsService.selectByProjectIdAndUserId(projectId, userId);
        if (pa != null) {
            r.setApplyStatus(pa.getStatus());
        }
        return r;
    }

    public Boolean likeProject(Long projectId, Long id, Boolean liked) {
        return projectLikeService.likeProject(projectId, id, liked);
    }

    public Boolean favoriteProject(Long projectId, Long id, Boolean liked) {
        return projectFavoriteService.favoriteProject(projectId, id, liked);
    }

    public Boolean comment(UserCommentProjectReq req, Long userId, String name) {
        return projectCommentService.comment(req.getProjectId(), userId, name, req.getContent(), req.getReplyTo(), req.getFirstLevelCommonId());
    }

    public List<ProjectCommentResp> commentShow(Long projectId, Long currentUserId) {
        List<ProjectComment> all = projectCommentService.selectByProjectId(projectId)
                .stream()
                .collect(Collectors.toList());
        List<ProjectCommentLike> likes = projectCommentLikeService.selectByProjectId(projectId);


        // 1. 映射全部评论
        Map<Long, List<ProjectCommentLike>> commentId2LikeListMap = likes.stream().collect(Collectors.groupingBy(ProjectCommentLike::getCommentId));

        // 构建第一层评论（replyTo=0）
        Map<Long, ProjectCommentResp> firstLevelMap = all.stream()
                .filter(c -> c.getReplyTo() < 0)
                .collect(Collectors.toMap(
                        ProjectComment::getId,
                        c -> convertToVO(c, currentUserId, commentId2LikeListMap)
                ));

        // 构建第二层评论（first_level_common_id对应第一层ID）
        Map<Long, List<ProjectCommentResp>> secondLevelMap = all.stream()
                .filter(c -> c.getReplyTo() > 0)
                .collect(Collectors.groupingBy(
                        ProjectComment::getFirstLevelCommonId,
                        Collectors.mapping(
                                c -> convertToVO(c, currentUserId, commentId2LikeListMap),
                                Collectors.toList()
                        )
                ));

        // 组装评论树
        List<ProjectCommentResp> result = new ArrayList<>();
        for (Long firstCommentId : firstLevelMap.keySet()) {
            ProjectCommentResp firstLevel = firstLevelMap.get(firstCommentId);
            if (secondLevelMap.containsKey(firstCommentId)) {
                List<ProjectCommentResp> secondList = secondLevelMap.get(firstCommentId);
                if (secondList != null) {
                    List<ProjectCommentResp> collect = secondList.stream()
                            .sorted(Comparator.comparing(ProjectCommentResp::getCreatedAt))
                            .collect(Collectors.toList());
                    firstLevel.setReplies(collect);
                }
            }
            result.add(firstLevel);
        }

        return result.stream()
                .sorted(Comparator.comparing(ProjectCommentResp::getCreatedAt))
                .collect(Collectors.toList());
    }

    private ProjectCommentResp convertToVO(ProjectComment comment, Long currentUserId, Map<Long, List<ProjectCommentLike>> commentId2LikeListMap) {
        ProjectCommentResp vo = new ProjectCommentResp();
        vo.setId(comment.getId());
        vo.setProjectId(comment.getProjectId());
        vo.setContent(comment.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) ? "本条评论已被用户删除～" : comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyTo(comment.getReplyTo());
        vo.setDeleted(comment.getIsDeleted() != 0);
        vo.setLikes(commentId2LikeListMap.containsKey(comment.getId()) ? commentId2LikeListMap.get(comment.getId()).size() : 0);
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

    public Boolean commentLike(Long commentId, Long projectId, Long userId) {
        ProjectCommentLike l = projectCommentLikeService.selectByCommentIdAndUserId(commentId, projectId, userId);
        if (l != null) {
            throw new ValidationException("已点赞，无需重复操作！");
        }
        return projectCommentLikeService.insert(commentId, projectId, userId);
    }

    public Boolean concernPublisher(ConcernPublisherReq req, Long id) {
        UserFollow entity = new UserFollow();
        entity.setFollowerId(id);
        entity.setFolloweeId(req.getFolloweeId());
        entity.setIsMutual(UserEnums.FollowEnum.No.getCode());
        return userFollowService.save(entity);
    }

    public Boolean concernPublisherCancel(ConcernPublisherCancelReq req, Long userId) {
        UserFollow useFollwe = userFollowService.selectUserByUserId(userId, req.getFolloweeId());
        if (useFollwe.getIsMutual().equals(UserEnums.FollowEnum.Yes.getCode())) {
            userFollowService.updateIsMutual(req.getFolloweeId(), userId, UserEnums.FollowEnum.No.getCode());
        }
        return userFollowService.removeUserByUserId(userId, req.getFolloweeId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean createFindCollage(CreateFindCollageReq req, Long userId) {
        Projects pd = new Projects();
        pd.setName(req.getName());
        pd.setCategory(req.getCategory());
        pd.setDescription(req.getDescription());
        pd.setDifficulty(req.getDifficulty());
        pd.setImageUrl(req.getImageUrl());
        pd.setCreatedBy(userId);
        pd.setModifiedBy(userId);
        pd.setStatus(ProjectEnum.ProjectStatusEnum.WAITING.getCode());

        boolean save = projectService.save(pd);
        if (!save) {
            throw new ValidationException("保存失败");
        }

        ProjectsDetail pdd = new ProjectsDetail();
        pdd.setProjectsId(pd.getId());
        pdd.setNeedMember(req.getNeedMember());
        pdd.setMemberNum(req.getMemberNum());
        pdd.setSteps(req.getSteps());
        pdd.setTools(req.getTools());
        pdd.setTimePerDay(req.getTimePerDay());
        pdd.setIncomeEstimate(req.getIncomeEstimate());
        pdd.setTargetAudience(req.getTargetAudience());
        pdd.setRiskWarning(req.getRiskWarning());
        pdd.setIsRemote(req.getIsRemote());
        pdd.setIsFreeEntry(req.getIsFreeEntry());
        pdd.setTags(req.getTags());
        pdd.setCreatedBy(userId);
        pdd.setModifiedBy(userId);
        boolean save1 = projectsDetailService.save(pdd);
        if (!save1) {
            throw new ValidationException("保存失败");
        }

        ProjectMembers pm = new ProjectMembers();
        pm.setProjectId(pd.getId());
        pm.setUserId(userId);
        pm.setJoinTime(new Date());
        pm.setRole(ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode());
        pm.setCreatedBy(userId);
        pm.setModifiedBy(userId);
        boolean save2 = projectMembersService.save(pm);
        if (!save2) {
            throw new ValidationException("保存失败");
        }
        return true;
    }

    public Boolean updateProjectOfMyShow(ProjectOfMyShowUpdateReq req, Long userId) {
        // 字段校验
        if (StringUtils.isAnyBlank(req.getSkills(), req.getTime(), req.getAudience(), req.getResources())
                || req.getStatus() == null) {
            throw new ValidationException("所有字段必须填写");
        }

        // 查询现有数据
        AccountShow entity = accountShowService.getByUserId(userId);
        boolean isNew = false;
        if (entity == null) {
            entity = new AccountShow();
            entity.setUserId(userId);
            entity.setCreatedBy(userId);
            isNew = true;
        }

        // 更新字段
        entity.setSkills(req.getSkills());
        entity.setTimePerDay(req.getTime());
        entity.setAudience(req.getAudience());
        entity.setResources(req.getResources());
        entity.setStatus(req.getStatus());
        entity.setModifiedBy(userId);
        entity.setModifiedAt(new Date());

        return isNew ? accountShowService.save(entity) : accountShowService.updateById(entity);
    }

    public ProjectOfMyShowGetResp getProjectOfMyShow(Long userId) {
        AccountShow byUserId = accountShowService.getByUserId(userId);
        ProjectOfMyShowGetResp build = new ProjectOfMyShowGetResp();
        build.setId(byUserId.getId());
        build.setAudience(byUserId.getAudience());
        build.setResources(byUserId.getResources());
        build.setSkills(byUserId.getSkills());
        build.setStatus(byUserId.getStatus());
        build.setTimePerDay(byUserId.getTimePerDay());
        return build;
    }

    public Boolean changeShowStatus(Long projectShowId, Integer status, Long userId) {
        AccountShow as = accountShowService.getById(projectShowId);
        if (!as.getCreatedBy().equals(userId)) {
            return false;
        }
        AccountShow accountShow = new AccountShow();
        accountShow.setStatus(status);
        return accountShowService.update(accountShow, new QueryWrapper<AccountShow>().eq("id", projectShowId));
    }

    public Page<ProjectOfMyShowGetResp> projectShowList(Page<AccountShow> page, ProjectShowListReq req) {
        Page<AccountShow> list = accountShowService.getProjectShowList(page);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> userIds = list.getRecords().stream().map(v -> v.getUserId()).collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<ProjectOfMyShowGetResp> collect = list.getRecords().stream().map(v -> {
            ProjectOfMyShowGetResp projectsResp = new ProjectOfMyShowGetResp();
            BeanUtils.copyProperties(v, projectsResp);

            projectsResp.setUserId(v.getUserId());
            if (userId2UserInfoMap.containsKey(v.getUserId())) {
                projectsResp.setUserName(userId2UserInfoMap.get(v.getUserId()).getNickname());
            }
            return projectsResp;
        }).collect(Collectors.toList());

        Page<ProjectOfMyShowGetResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean applyJoinProject(Long projectId, Long userid) {
        ProjectsDetail pd = projectsDetailService.selectByProjectId(projectId);
        if (pd == null) {
            throw new ValidationException("项目不存在");
        }
        if (pd.getCreatedBy().equals(userid)) {
            throw new ValidationException("不能申请加入自己的项目");
        }
        if (ProjectEnum.IsNeedMemberEnum.NO_NEED_MEMBER.getCode().equals(pd.getNeedMember())) {
            throw new ValidationException("该项目不需要成员");
        }

        List<ProjectMembers> members = projectMembersService.selectByProjectIdAndNeRole(projectId, ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode());
        if (!CollectionUtils.isEmpty(members) && pd.getMemberNum() >= members.size()) {
            throw new ValidationException("该项目已满员");
        }

//        List<ProjectMembers> joinedMembers = projectMembersService.selectByUserId(userid);
//        if (!CollectionUtils.isEmpty(joinedMembers) && joinedMembers.size()>=5) {
//            throw new ValidationException("您已加入5个项目,无法再加");
//        }
        ProjectApplications entity = new ProjectApplications();
        entity.setProjectId(projectId);
        entity.setUserId(userid);
        entity.setStatus(ProjectEnum.ProjectApplyStatusEnum.WAIT_AUDIT.getCode());
        entity.setApplyTime(new Date());
        entity.setProcessedBy(pd.getCreatedBy());
        return projectApplicationsService.save(entity);
    }
}
