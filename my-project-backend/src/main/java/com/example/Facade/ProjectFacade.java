package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.AsyncTaskUtil;
import com.example.constants.CommonConstant;
import com.example.entity.dto.*;
import com.example.entity.req.*;
import com.example.entity.resp.*;
import com.example.enums.CommonEnum;
import com.example.enums.MessageEnums;
import com.example.enums.ProjectEnum;
import com.example.enums.UserEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProjectFacade {

    @Resource
    ProjectWatchService projectWatchService;
    @Resource
    MessageFacade messageFacade;
    @Resource
    ProjectApplicationsService projectApplicationsService;
    @Resource
    ProjectReviewService projectReviewService;
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

    public ProjectUpdateResp detailForUpdate(Long projectId) {
        ProjectUpdateResp r = new ProjectUpdateResp();

        Projects projectDown = projectService.selectByProjectId(projectId);
        BeanUtils.copyProperties(projectDown, r);

        ProjectsDetail projectsDetail = projectsDetailService.selectByProjectId(projectId);
        BeanUtils.copyProperties(projectsDetail, r);

        r.setTargetAudienceName(CommonEnum.UserTypeEnum.getByCode(projectsDetail.getTargetAudience()));
        return r;
    }

    public ProjectsDetailResp selectByProjectId(Long projectId, Long userId) {
        Projects projectDown = projectService.selectByProjectId(projectId);
        if (projectDown == null) {
            return new ProjectsDetailResp();
        }

        ProjectsDetailResp r = new ProjectsDetailResp();
        ProjectsDetail project = projectsDetailService.selectByProjectId(projectId);
        if (project != null) {
            BeanUtils.copyProperties(project, r);
            r.setName(projectDown.getName());
            r.setStatus(projectDown.getStatus());
            r.setStatusName(ProjectEnum.ProjectStatusEnum.getByCode(projectDown.getStatus()));
            r.setLikeCount(projectLikeService.selectCountByProjectId(projectId));
            r.setFavoriteCount(projectFavoriteService.selectCountByProjectId(projectId));
            r.setTimePerDay(project.getTimePerDay() + "小时/天");
            r.setIncomeEstimate(project.getIncomeEstimateMin() + "-" + project.getIncomeEstimateMax() + "元/天");
            r.setTargetAudience(CommonEnum.UserTypeEnum.getByCode(project.getTargetAudience()));
            r.setImageUrl(projectDown.getImageUrl());
            Account account = accountService.selectById(project.getCreatedBy());
            r.setCreatorName(account.getNickname());
            r.setSecrecyId(account.getSecrecyId());
            if (StringUtils.isNotBlank(r.getTags())) {
                try {
                    String tags = Arrays.stream(r.getTags().split(",")).map(v -> CommonEnum.LabelEnums.getByCode(Integer.valueOf(v))).collect(Collectors.joining(","));
                    r.setTags(tags);
                } catch (Exception e) {
                    log.error("标签转换错误:{}", r.getTags(), e);
                }
            }

            if (userId != null) {
                Boolean myLike = projectLikeService.selectByProjectIdAndUserId(projectId, userId);
                r.setMyLike(myLike);
                Boolean myFavorite = projectFavoriteService.selectByProjectIdAndUserId(projectId, userId);
                r.setMyFavorite(myFavorite);

                Boolean followed = userFollowService.selectByUserIdAndFollowedId(userId, project.getCreatedBy());
                r.setFollowed(followed);

                ProjectApplications pa = projectApplicationsService.selectByProjectIdAndUserId(projectId, userId);
                if (pa != null) {
                    r.setApplyStatus(pa.getStatus());
                }
            }
        }

        return r;
    }

    public Boolean likeProject(Long projectId, Long id, Boolean liked) {
        Boolean result = projectLikeService.likeProject(projectId, id, liked);
        if (liked) {
            messageFacade.createMessageOfLike(projectId, projectId, id, true);
        } else {
            messageFacade.deletedMessageOfLike(projectId, projectId, id, true);
        }
        return result;
    }

    public Boolean favoriteProject(Long projectId, Long id, Boolean liked) {
        return projectFavoriteService.favoriteProject(projectId, id, liked);
    }

    public Boolean comment(UserCommentProjectReq req, Long userId, String name) {
        Long commentId = projectCommentService.comment(req.getProjectId(), userId, name, req.getContent(), req.getReplyTo(), req.getFirstLevelCommonId());
        if (commentId != null) {
            messageFacade.createMessageOfComment(req.getProjectId(), commentId, req.getReplyTo(), userId);
            return true;
        } else {
            return false;
        }
    }

    public List<ProjectCommentResp> commentShow(Long projectId, Long currentUserId) {
        List<ProjectComment> all = projectCommentService.selectByProjectId(projectId)
                .stream()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(all)) {
            return new ArrayList<>();
        }
        List<ProjectCommentLike> likes = projectCommentLikeService.selectByProjectId(projectId);


        List<Long> userIds = all.stream().map(v -> v.getUserId()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        // 1. 映射全部评论
        Map<Long, List<ProjectCommentLike>> commentId2LikeListMap = likes.stream().collect(Collectors.groupingBy(ProjectCommentLike::getCommentId));

        // 构建第一层评论（replyTo=0）
        Map<Long, ProjectCommentResp> firstLevelMap = all.stream()
                .filter(c -> c.getReplyTo() < 0)
                .collect(Collectors.toMap(
                        ProjectComment::getId,
                        c -> convertToVO(c, currentUserId, commentId2LikeListMap, userId2UserInfoMap)
                ));

        // 构建第二层评论（first_level_common_id对应第一层ID）
        Map<Long, List<ProjectCommentResp>> secondLevelMap = all.stream()
                .filter(c -> c.getReplyTo() > 0)
                .collect(Collectors.groupingBy(
                        ProjectComment::getFirstLevelCommonId,
                        Collectors.mapping(
                                c -> convertToVO(c, currentUserId, commentId2LikeListMap, userId2UserInfoMap),
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

    private ProjectCommentResp convertToVO(ProjectComment comment, Long currentUserId, Map<Long, List<ProjectCommentLike>> commentId2LikeListMap, Map<Long, Account> userId2UserInfoMap) {
        ProjectCommentResp vo = new ProjectCommentResp();
        vo.setId(comment.getId());
        vo.setProjectId(comment.getProjectId());
        vo.setContent(comment.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) ? "本条评论已被用户删除～" : comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyTo(comment.getReplyTo());
        vo.setReplyToName(comment.getReplyToUsername());
        vo.setDeleted(comment.getIsDeleted() != 0);
        vo.setLikes(commentId2LikeListMap.containsKey(comment.getId()) ? commentId2LikeListMap.get(comment.getId()).size() : 0);
        vo.setIsMine(comment.getUserId().equals(currentUserId));
        vo.setFirstLevelCommonId(comment.getFirstLevelCommonId());
        vo.setSecrecyId(userId2UserInfoMap.get(comment.getUserId()).getSecrecyId());

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
        Boolean result = projectCommentLikeService.insert(commentId, projectId, userId);
        messageFacade.createMessageOfLike(projectId, commentId, userId, false);
        return result;
    }

    public Boolean concernPublisher(ConcernPublisherReq req, Long id) {
        Boolean exist = userFollowService.selectByUserIdAndFollowedId(id, req.getFolloweeId());
        if (exist) {
            return true;
        }
        UserFollow entity = new UserFollow();
        entity.setFollowerId(id);
        entity.setFolloweeId(req.getFolloweeId());
        entity.setIsMutual(UserEnums.FollowEnum.No.getCode());
        Boolean result = userFollowService.save(entity);
        messageFacade.createMessageOfPublisher(req.getFolloweeId(), id);
        return result;
    }

    public Boolean concernPublisherCancel(ConcernPublisherCancelReq req, Long userId) {
        UserFollow useFollwe = userFollowService.selectUserByUserId(userId, req.getFolloweeId());
        if (useFollwe == null) {
            return true;
        }
        if (useFollwe.getIsMutual().equals(UserEnums.FollowEnum.Yes.getCode())) {
            userFollowService.updateIsMutual(req.getFolloweeId(), userId, UserEnums.FollowEnum.No.getCode());
        }
        Boolean result = userFollowService.removeUserByUserId(userId, req.getFolloweeId());
        messageFacade.deletedMessageOfPublisher(userId, req.getFolloweeId(), MessageEnums.MessageType.PUBLISHER.getCode());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean createFindCollage(CreateFindCollageReq req, Long userId) {
        // 判断是更新还是创建
        boolean isUpdate = req.getId() != null;

        Projects pd;
        ProjectsDetail pdd;

        if (isUpdate) {
            // 更新逻辑
            // 1. 更新Projects表
            pd = projectService.getById(req.getId());
            if (pd == null) {
                throw new ValidationException("项目不存在");
            }
            pd.setStatus(ProjectEnum.ProjectStatusEnum.WAITING.getCode());
            pd.setName(req.getName());
            pd.setFirstCategory(req.getFirstCategory());
            pd.setSecondCategory(req.getSecondCategory());
            pd.setDescription(req.getDescription());
            pd.setDifficulty(req.getDifficulty());
            pd.setImageUrl(req.getImageUrl());
            pd.setModifiedBy(userId);
            boolean updateProject = projectService.updateById(pd);
            if (!updateProject) {
                throw new ValidationException("更新项目失败");
            }

            // 2. 更新ProjectsDetail表
            pdd = projectsDetailService.lambdaQuery()
                    .eq(ProjectsDetail::getProjectsId, req.getId())
                    .one();
            if (pdd == null) {
                throw new ValidationException("项目详情不存在");
            }
            pdd.setNeedMember(req.getNeedMember());
            pdd.setMemberNum(req.getMemberNum());
            pdd.setSteps(req.getSteps());
            pdd.setTools(req.getTools());
            pdd.setTimePerDay(req.getTimePerDay());
            pdd.setIncomeEstimateMin(req.getIncomeEstimateMin());
            pdd.setIncomeEstimateMax(req.getIncomeEstimateMax());
            pdd.setTargetAudience(req.getTargetAudience());
            pdd.setRiskWarning(req.getRiskWarning());
            pdd.setTags(req.getTags());
            pdd.setModifiedBy(userId);
            boolean updateDetail = projectsDetailService.updateById(pdd);
            if (!updateDetail) {
                throw new ValidationException("更新项目详情失败");
            }

            // 3. 检查ProjectMembers表，确保用户仍然是管理员
            long count = projectMembersService.lambdaQuery()
                    .eq(ProjectMembers::getProjectId, req.getId())
                    .eq(ProjectMembers::getUserId, userId)
                    .eq(ProjectMembers::getRole, ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode())
                    .count();
            if (count == 0) {
                throw new ValidationException("用户没有权限更新此项目");
            }
        } else {
            // 创建逻辑（保持原有逻辑）
            // 1. 保存Projects表
            pd = new Projects();
            pd.setName(req.getName());
            pd.setFirstCategory(req.getFirstCategory());
            pd.setSecondCategory(req.getSecondCategory());
            pd.setDescription(req.getDescription());
            pd.setDifficulty(req.getDifficulty());
            pd.setImageUrl(req.getImageUrl());
            pd.setCreatedBy(userId);
            pd.setModifiedBy(userId);
            pd.setStatus(ProjectEnum.ProjectStatusEnum.WAITING.getCode());
            boolean saveProject = projectService.save(pd);
            if (!saveProject) {
                throw new ValidationException("保存项目失败");
            }

            // 2. 保存ProjectsDetail表
            pdd = new ProjectsDetail();
            pdd.setProjectsId(pd.getId());
            pdd.setNeedMember(req.getNeedMember());
            pdd.setMemberNum(req.getMemberNum());
            pdd.setSteps(req.getSteps());
            pdd.setTools(req.getTools());
            pdd.setTimePerDay(req.getTimePerDay());
            pdd.setIncomeEstimateMin(req.getIncomeEstimateMin());
            pdd.setIncomeEstimateMax(req.getIncomeEstimateMax());
            pdd.setTargetAudience(req.getTargetAudience());
            pdd.setRiskWarning(req.getRiskWarning());
            pdd.setTags(req.getTags());
            pdd.setCreatedBy(userId);
            pdd.setModifiedBy(userId);
            boolean saveDetail = projectsDetailService.save(pdd);
            if (!saveDetail) {
                throw new ValidationException("保存项目详情失败");
            }

            // 3. 保存ProjectMembers表
            ProjectMembers pm = new ProjectMembers();
            pm.setProjectId(pd.getId());
            pm.setUserId(userId);
            pm.setJoinTime(new Date());
            pm.setRole(ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode());
            pm.setCreatedBy(userId);
            pm.setModifiedBy(userId);
            boolean saveMember = projectMembersService.save(pm);
            if (!saveMember) {
                throw new ValidationException("保存项目成员失败");
            }
        }

        return true;
    }

    public Boolean updateProjectOfMyShow(ProjectOfMyShowUpdateReq req, Long userId) {
        // 字段校验
        if (StringUtils.isAnyBlank(req.getResources())
                || req.getStatus() == null || !StringUtils.isNotBlank(req.getSkills()) || req.getTime() == null || null == req.getAudience()) {
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
        if (byUserId == null) {
            return null;
        }
        ProjectOfMyShowGetResp build = new ProjectOfMyShowGetResp();
        build.setId(byUserId.getId());
        build.setAudience(byUserId.getAudience());
        build.setAudienceName(CommonEnum.UserTypeEnum.getByCode(byUserId.getAudience()));
        build.setResources(byUserId.getResources());
        build.setSkills(byUserId.getSkills());

        if (StringUtils.isNotBlank(byUserId.getSkills())) {
            String skillNames = Arrays.stream(byUserId.getSkills().split(",")).map(z -> CommonEnum.IndustryCategory.getNameByCode(Integer.valueOf(z))).collect(Collectors.joining(","));
            build.setSkillNames(skillNames);
        }
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
                projectsResp.setSecrecyId(userId2UserInfoMap.get(v.getUserId()).getSecrecyId());
            }

            projectsResp.setAudienceName(CommonEnum.UserTypeEnum.getByCode(v.getAudience()));
            String skillNames = Arrays.stream(v.getSkills().split(",")).map(s -> CommonEnum.IndustryCategory.getNameByCode(Integer.parseInt(s))).collect(Collectors.joining(","));
            projectsResp.setSkillNames(skillNames);
            return projectsResp;
        }).collect(Collectors.toList());

        Page<ProjectOfMyShowGetResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean applyJoinProject(Long projectId, String message, Long userid) {
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

        if (projectFull(projectId)) {
            throw new ValidationException("该项目已满员");
        }
        List<ProjectMembers> members = projectMembersService.selectByProjectId(projectId, ProjectEnum.MemberStatusEnum.IN.getCode());
        List<ProjectMembers> collect = members.stream().filter(v -> v.getUserId().equals(userid)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            throw new ValidationException("已加入该项目");
        }

        ProjectApplications p = projectApplicationsService.selectByProjectIdAndUserId(projectId, userid);
        if (p != null) {
            if (p.getStatus().equals(ProjectEnum.ProjectApplyStatusEnum.WAIT_AUDIT.getCode())) {
                throw new ValidationException("请勿重复申请");
            } else {
                projectApplicationsService.removeById(p.getId());
            }
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
        entity.setMessage(message);
        boolean save = projectApplicationsService.save(entity);

        AsyncTaskUtil.execute(() -> messageFacade.createApprove(pd.getCreatedBy(), userid, projectId));
        return save;
    }


    public Boolean cancelApplyJoinProject(Long projectId, Long userId) {
        ProjectsDetail pd = projectsDetailService.selectByProjectId(projectId);
        if (pd == null) {
            throw new ValidationException("项目不存在");
        }
        if (pd.getCreatedBy().equals(userId)) {
            throw new ValidationException("不能操作自己的项目");
        }
        return projectApplicationsService.remove(new UpdateWrapper<ProjectApplications>().eq("project_id", projectId).eq("user_id", userId));
    }

    public MyCountResp getMyCount(Long userId) {
        MyCountResp result = new MyCountResp();
        List<ProjectApplications> projectApplications = projectApplicationsService.selectByProcessedBy(userId, ProjectEnum.ProjectApplyStatusEnum.WAIT_AUDIT.getCode());
        result.setApplyCount(projectApplications.size());

        List<ProjectApplications> projectApplications1 = projectApplicationsService.selectByUserId(userId, ProjectEnum.ProjectApplyStatusEnum.WAIT_AUDIT.getCode());
        result.setApplicationCount(projectApplications1.size());
        return result;
    }

    public Page<MyApplyListResp> myApplyList(Page<ProjectApplications> page, MyApplyListReq req, Long userId) {
        Page<ProjectApplications> list = projectApplicationsService.myApplyList(page, userId, ProjectEnum.ProjectApplyStatusEnum.WAIT_AUDIT.getCode());

        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> projectIds = list.getRecords().stream().map(v -> v.getProjectId()).collect(Collectors.toList());

        List<Projects> projectList = projectService.selectByProjectIds(projectIds);
        Map<Long, Projects> projectId2ProjectsMap = projectList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v, (l1, l2) -> l2));

        List<Long> userIds = list.getRecords().stream().map(v -> v.getUserId()).collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<AccountShow> showList = accountShowService.selectByUserIds(userIds);
        Map<Long, AccountShow> userId2ShowInfoMap = showList.stream().collect(Collectors.toMap(v -> v.getUserId(), v -> v));

        List<MyApplyListResp> collect = list.getRecords().stream().map(v -> {
            MyApplyListResp p = new MyApplyListResp();
            p.setId(v.getId());
            if (projectId2ProjectsMap.containsKey(v.getProjectId())) {
                Projects project = projectId2ProjectsMap.get(v.getProjectId());
                p.setProjectName(project.getName());
                p.setDescription(project.getDescription());
            }

            p.setUserId(v.getUserId());
            if (userId2UserInfoMap.containsKey(v.getUserId())) {
                p.setUserName(userId2UserInfoMap.get(v.getUserId()).getNickname());
                p.setAvatarUrl(userId2UserInfoMap.get(v.getUserId()).getAvatarUrl());
                p.setSecrecyId(userId2UserInfoMap.get(v.getUserId()).getSecrecyId());
            }

            if (userId2ShowInfoMap.containsKey(v.getUserId())) {
                AccountShow as = userId2ShowInfoMap.get(v.getUserId());
                p.setTimePerDayStr(as.getTimePerDay() + CommonConstant.TIME_PER_DAY);
                p.setAudience(CommonEnum.UserTypeEnum.getByCode(as.getAudience()));
                if (StringUtils.isNotBlank(as.getSkills())) {
                    String skillNames = Arrays.stream(as.getSkills().split(",")).map(z -> CommonEnum.IndustryCategory.getNameByCode(Integer.valueOf(z))).collect(Collectors.joining(","));
                    p.setSkillsName(skillNames);
                }
                p.setResources(as.getResources());
            }

            p.setStatus(v.getStatus());
            p.setApplyTime(v.getApplyTime());
            p.setMessage(v.getMessage());
            return p;
        }).collect(Collectors.toList());

        Page<MyApplyListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;

    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean approveApply(ApproveApplyReq req, Long userId) {
        ProjectApplications pa = projectApplicationsService.getById(req.getId());
        if (pa == null) {
            throw new ValidationException("申请不存在");
        }
        Projects project = projectService.selectByProjectIdAndStatus(pa.getProjectId(), null);

        boolean saveOrUpdate = false;
        ProjectMembers pm = projectMembersService.selectByProjectIdAndUserId(pa.getProjectId(), pa.getUserId());
        if (pm != null) {
            if (pm.getStatus().equals(ProjectEnum.MemberStatusEnum.IN.getCode())) {
                throw new ValidationException("用户已加入项目");
            } else {
                pm.setJoinTime(new Date());
                pm.setRole(ProjectEnum.ProjectMemberRoleEnum.NORMAL.getCode());
                pm.setStatus(ProjectEnum.MemberStatusEnum.IN.getCode());
                saveOrUpdate = projectMembersService.updateById(pm);
            }
        } else {
            ProjectMembers entity = new ProjectMembers();
            entity.setProjectId(project.getId());
            entity.setUserId(pa.getUserId());
            entity.setJoinTime(new Date());
            entity.setRole(ProjectEnum.ProjectMemberRoleEnum.NORMAL.getCode());
            saveOrUpdate = projectMembersService.save(entity);
        }


        if (!saveOrUpdate) {
            throw new ValidationException("内部错误，加入失败");
        }
        projectApplicationsService.updateStatus(req.getId(), ProjectEnum.ProjectApplyStatusEnum.APPROVED.getCode(), pa.getUserId());
        if (Boolean.TRUE.equals(projectFull(project.getId()))) {
            //更新其他人的申请状态
            projectApplicationsService.updateStatusByProjectId(ProjectEnum.ProjectApplyStatusEnum.REJECTED.getCode(), project.getId(), ProjectEnum.ProjectApplyStatusEnum.APPROVED.getCode());
            //更改项目状态
            projectService.updateStatus(project.getId(), ProjectEnum.ProjectStatusEnum.FULL.getCode(), null, userId);
        }
        AsyncTaskUtil.execute(() -> messageFacade.createApprovePass(pa.getUserId(), userId, project.getId()));
        return true;
    }

    /**
     * 项目已经满员
     *
     * @return boolean
     */
    public Boolean projectFull(Long projectId) {
        ProjectsDetail pd = projectsDetailService.selectByProjectId(projectId);
        List<ProjectMembers> members = projectMembersService.selectByProjectId(projectId, ProjectEnum.MemberStatusEnum.IN.getCode());
        if (!CollectionUtils.isEmpty(members) && pd.getMemberNum() <= (members.size() - 1)) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean rejectApply(ApproveApplyReq req, Long id) {
        ProjectApplications pa = projectApplicationsService.getById(req.getId());
        Integer result = projectApplicationsService.updateStatus(req.getId(), ProjectEnum.ProjectApplyStatusEnum.REJECTED.getCode(), id);
        if (result > 0) {
            AsyncTaskUtil.execute(() -> messageFacade.createApproveRefuse(pa.getUserId(), pa.getProcessedBy(), pa.getProjectId()));
        }
        return result == 1;
    }

    public Page<MyApplicationListResp> myApplicationList(Page<ProjectApplications> page, MyApplyListReq req, Long userId) {
        Page<ProjectApplications> list = projectApplicationsService.myApplicationList(page, userId);


        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> projectIds = list.getRecords().stream().map(v -> v.getProjectId()).collect(Collectors.toList());

        List<Projects> projectList = projectService.selectByProjectIds(projectIds);
        Map<Long, Projects> projectId2ProjectsMap = projectList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v, (l1, l2) -> l2));

        List<MyApplicationListResp> collect = list.getRecords().stream().map(v -> {
            MyApplicationListResp p = new MyApplicationListResp();
            p.setId(v.getId());
            if (projectId2ProjectsMap.containsKey(v.getProjectId())) {
                Projects project = projectId2ProjectsMap.get(v.getProjectId());
                p.setProjectId(project.getId());
                p.setProjectName(project.getName());
            }

            p.setStatus(v.getStatus());
            p.setApplyTime(v.getApplyTime());
            return p;
        }).collect(Collectors.toList());

        Page<MyApplicationListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<AdminApplyListResp> adminApproveList(Page<Projects> page, MyApplyListReq req, Long userId) {
        Page<Projects> list = projectService.getProjectsPageByStatus(page, ProjectEnum.ProjectStatusEnum.WAITING.getCode());
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<AdminApplyListResp> collect = list.getRecords().stream().map(v -> {
            AdminApplyListResp p = new AdminApplyListResp();
            p.setId(v.getId());
            p.setProjectName(v.getName());
            p.setStatus(v.getStatus());
            p.setApplyTime(v.getCreatedAt());
            return p;
        }).collect(Collectors.toList());

        Page<AdminApplyListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Page<MatchUserResp> matchUser(Page<Projects> page, MatchUserReq req) {
        Long projectId = req.getProjectId();
        Projects p = projectService.selectByProjectId(projectId);
        ProjectsDetail pd = projectsDetailService.selectByProjectId(projectId);

        List<AccountShow> userList = accountShowService.selectAllList(UserEnums.AccountShowEnum.Yes.getCode());
        Map<Long, AccountShow> userId2UseShowMap = userList.stream().collect(Collectors.toMap(AccountShow::getUserId, v -> v));
        List<Account> userInfoList = accountService.selectFildByUserId();

        List<ProjectMembers> pm = projectMembersService.selectByProjectId(projectId, null);
        Map<Long, Integer> userId2UserStatusMap = pm.stream().collect(Collectors.toMap(v -> v.getUserId(), v -> v.getStatus(), (l1, l2) -> l2));

        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(Account::getId, v -> v));
        // 2. 遍历计算匹配度
        List<MatchUserResp> matchUserRespList = new ArrayList<>();
        for (AccountShow user : userList) {
            int score = 0;

            // 技能匹配度（50分）
            Set<Integer> projectSkills = new HashSet<>(Arrays.asList(p.getFirstCategory(), p.getSecondCategory()));
            Set<Integer> userSkills = Arrays.stream(user.getSkills().split(",")).map(Integer::parseInt).collect(Collectors.toSet());
            projectSkills.retainAll(userSkills);
            int skillMatchCount = projectSkills.size();
            score += Math.min(skillMatchCount * 10, 50); // 每个技能10分，最多50

            // audience匹配（20分）
            if (pd.getTargetAudience() == null) {
                score += 20;
            } else {
                if (Objects.equals(pd.getTargetAudience(), user.getAudience())) {
                    score += 20;
                }
            }

            // 资源匹配（20分）
//            if (!StringUtils.isEmpty(pd.getResources()) &&
//                    user.getResources().contains(pd.getResources())) {
//                score += 20;
//            }
            if (StringUtils.isNotBlank(user.getResources())) {
                score += 5;
            }

            // 投入时间匹配（10分）
            if (user.getTimePerDay() >= pd.getTimePerDay()) {
                score += 25;
            } else if (user.getTimePerDay() >= pd.getTimePerDay() / 2) {
                score += 15;
            } else if (user.getTimePerDay() >= pd.getTimePerDay() / 4) {
                score += 5;
            }

            // 封装结果
            MatchUserResp resp = new MatchUserResp();
            resp.setUserId(user.getUserId());
            if (userId2UserInfoMap.containsKey(user.getUserId())) {
                resp.setUsername(userId2UserInfoMap.get(user.getUserId()).getNickname());
                resp.setSecrecyId(userId2UserInfoMap.get(user.getUserId()).getSecrecyId());
                resp.setAvatarUrl(userId2UserInfoMap.get(user.getUserId()).getAvatarUrl());
                resp.setSex(userId2UserInfoMap.get(user.getUserId()).getSex());
                resp.setProvince(userId2UserInfoMap.get(user.getUserId()).getProvince());
                resp.setCity(userId2UserInfoMap.get(user.getUserId()).getCity());
                resp.setCounty(userId2UserInfoMap.get(user.getUserId()).getCounty());
            }

            resp.setStatusOfUserInProject(userId2UserStatusMap.get(user.getUserId()));
            resp.setMatchScore(score);

            //private String audienceName;
            //    private String resources;
            //    private String skillNames;
            //    private Long timePerDay;
            resp.setAudienceName(CommonEnum.UserTypeEnum.getByCode(userId2UseShowMap.get(user.getUserId()).getAudience()));
            if (userId2UseShowMap.containsKey(user.getUserId())) {
                resp.setResources(userId2UseShowMap.get(user.getUserId()).getResources());
                String skillNames = Arrays.stream(userId2UseShowMap.get(user.getUserId()).getSkills().split(",")).map(z -> CommonEnum.IndustryCategory.getNameByCode(Integer.valueOf(z))).collect(Collectors.joining(","));
                resp.setSkillNames(skillNames);
                resp.setTimePerDay(userId2UseShowMap.get(user.getUserId()).getTimePerDay() + CommonConstant.TIME_PER_DAY);

            }
            matchUserRespList.add(resp);
        }

        // 3. 按匹配度降序排序
        matchUserRespList.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        // 4. 分页
        int current = (int) page.getCurrent();
        int size = (int) page.getSize();
        int start = (current - 1) * size;
        int end = Math.min(start + size, matchUserRespList.size());

        List<MatchUserResp> pageRecords = start >= matchUserRespList.size()
                ? Collections.emptyList()
                : matchUserRespList.subList(start, end);

        Page<MatchUserResp> resultPage = new Page<>(current, size, matchUserRespList.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
    }

    public Boolean cancelApply(CancelApproveReq req, Long userId) {
        ProjectApplications p = projectApplicationsService.selectByProjectIdAndUserId(req.getProjectId(), userId);
        if (p == null) {
            return false;
        }
        Boolean result = projectApplicationsService.updateStatus(p.getId(), ProjectEnum.ProjectApplyStatusEnum.CANCELED.getCode(), userId) > 0;
        if (result) {
            AsyncTaskUtil.execute(() -> messageFacade.deletedMessage(userId, p.getProcessedBy(), p.getProjectId(), MessageEnums.MessageType.JOIN_PROJECT.getCode()));
        }
        return result;
    }


    public Boolean adminApprovePass(Long projectId, Long userId) {
        return projectService.updateStatus(projectId, ProjectEnum.ProjectStatusEnum.PUBLISHING.getCode(), null, userId);
    }

    public Boolean adminApproveNo(Long projectId, String reason, Long userId) {
        return projectService.updateStatus(projectId, ProjectEnum.ProjectStatusEnum.NO.getCode(), reason, userId);
    }

    public Page<ShowHotProjectListPageResp> showHotProjectList(ShowHotProjectListPageReq req, Long userId) {
        //根据点赞*0.4 + 收藏* 0.4 +评论 *0.15 +  浏览*0.05排序
        Map<Long, Integer> projectId2FavoriteCount = projectFavoriteService.selectProjectId2FavoriteCount();
        Map<Long, Integer> projectId2LikeCount = projectLikeService.selectProjectId2LikeCount();
        Map<Long, Integer> projectId2CommentCount = projectCommentService.selectProjectId2CommentCount();

        Set<Long> projectIds = new HashSet<>(projectId2FavoriteCount.keySet());
        projectIds.addAll(projectId2LikeCount.keySet());

        if (CollectionUtils.isEmpty(projectIds)) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<ShowHotProjectListPageResp> paixuProjectIds = new ArrayList<>();
        List<Projects> projects = projectService.listByIds(projectIds);

        for (Projects p : projects) {
            Double score = 0d;
            if (!p.getStatus().equals(ProjectEnum.ProjectStatusEnum.PUBLISHING.getCode())) {
                continue;
            }
            if (!CollectionUtils.isEmpty(req.getCategoryIds())) {
                if (!req.getCategoryIds().contains(p.getFirstCategory()) && !req.getCategoryIds().contains(p.getSecondCategory())) {
                    continue;
                }
            }
            Integer favoriteCount = 0;
            if (projectId2FavoriteCount.containsKey(p.getId())) {
                favoriteCount = projectId2FavoriteCount.get(p.getId());
                score += favoriteCount * 0.4;
            }
            Integer likeCount = 0;
            if (projectId2LikeCount.containsKey(p.getId())) {
                likeCount = projectId2LikeCount.get(p.getId());
                score += likeCount * 0.4;
            }

            Integer commentCount = 0;
            if (projectId2CommentCount.containsKey(p.getId())) {
                commentCount = projectId2CommentCount.get(p.getId());
                score += commentCount * 0.2;
            }
            ShowHotProjectListPageResp r = new ShowHotProjectListPageResp();
            r.setProjectId(p.getId());
            r.setImageUrl(p.getImageUrl());
            r.setName(p.getName());
            r.setDescription(p.getDescription());
            r.setCreatedAt(p.getCreatedAt());
            r.setStatus(ProjectEnum.ProjectStatusEnum.getByCode(p.getStatus()));
            r.setFirstCategoryName(CommonEnum.IndustryCategory.getNameByCode(p.getFirstCategory()));
            r.setSecondCategoryName(CommonEnum.IndustryCategory.getNameByCode(p.getSecondCategory()));
            r.setLikeCount(likeCount);
            r.setCommentCount(commentCount);
            r.setFavoriteCount(favoriteCount);
            r.setScore(score);
            paixuProjectIds.add(r);
        }

        List<ShowHotProjectListPageResp> list = paixuProjectIds.stream()
                .sorted(Comparator.comparing(ShowHotProjectListPageResp::getScore).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        //根据分页参数，获取list的字集
        List<ShowHotProjectListPageResp> sub = list.stream().skip((req.getPage() - 1) * req.getSize()).limit(req.getSize()).collect(Collectors.toList());
        Page<ShowHotProjectListPageResp> result = Page.of(req.getPage(), req.getSize());
        result.setTotal(list.size());
        result.setRecords(sub);
        return result;
    }

    public Boolean addProjectWatch(Long projectId, Long userId) {
        ProjectWatch pw = projectWatchService.selectByProjectIdAndUserId(projectId, userId);
        if (pw != null) {
            return true;
        }
        return projectWatchService.insertOne(projectId, userId);
    }

    public Boolean closed(ProjectFinishReq req, Long userId) {
        Projects p = projectService.selectByProjectId(req.getProjectId());
        if (!p.getCreatedBy().equals(userId)) {
            throw new ValidationException("您没有权限关闭项目");
        }
        if (p.getStatus().equals(ProjectEnum.ProjectStatusEnum.CLOSED.getCode())) {
            return true;
        }
        return projectService.closeProject(req.getProjectId(), userId);
    }

    public Boolean hasEvaluate(Long projectId, Long userId) {
        List<ProjectMembers> pm = projectMembersService.selectByProjectId(projectId, ProjectEnum.MemberStatusEnum.IN.getCode());
        if (pm.size() < 2) {
            return true;
        }

        List<ProjectReview> prList = projectReviewService.selectByProjectIdAndFromUserId(projectId, userId);
        if (prList.size() < pm.size() - 1) {
            return false;
        }
        return true;
    }

    public Boolean evaluate(ProjectEvaluateReq req, Long userId) {
        ProjectMembers pm = projectMembersService.selectByProjectIdAndUserId(req.getProjectId(), req.getToUserId());
        if (pm == null || !pm.getStatus().equals(ProjectEnum.MemberStatusEnum.IN.getCode())) {
            throw new ValidationException("用户未加入");
        }

        Boolean saveOrUpdate = false;
        ProjectReview pr = projectReviewService.selectByProjectIdAndFromUserIdAndToUserId(req.getProjectId(), userId, req.getToUserId());
        if (pr != null) {
            pr.setScore(req.getScore());
            pr.setComment(req.getComment());
            pr.setModifiedBy(userId);
            pr.setModifiedAt(new Date());
            saveOrUpdate = projectReviewService.updateById(pr);
        } else {
            pr = new ProjectReview();
            pr.setProjectId(req.getProjectId());
            pr.setFromUserId(userId);
            pr.setToUserId(req.getToUserId());
            pr.setRole(pm.getRole());
            pr.setScore(req.getScore());
            pr.setComment(req.getComment());
            pr.setCreatedAt(new Date());
            pr.setCreatedBy(userId);
            pr.setModifiedBy(userId);
            pr.setModifiedAt(new Date());
            saveOrUpdate = projectReviewService.save(pr);
        }
        return saveOrUpdate;
    }

    public List<ProjectEvaluateListResp> getEvaluateList(Long projectId, Long userId) {
        List<ProjectReview> list = projectReviewService.selectByProjectIdAndFromUserId(projectId, userId);
        return list.stream().map(item -> {
            ProjectEvaluateListResp r = new ProjectEvaluateListResp();
            r.setComment(item.getComment());
            r.setScore(item.getScore());
            r.setUserId(item.getToUserId());
            r.setReviewId(item.getId());
            r.setProjectId(item.getProjectId());
            return r;
        }).collect(Collectors.toList());
    }

    public List<ProjectAvgEvaluateListResp> getAvgEvaluateList(Long projectId) {
        List<ProjectReview> list = projectReviewService.selectByProjectId(projectId);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Long> u1 = list.stream().map(v -> v.getFromUserId()).collect(Collectors.toList());
        List<Long> u2 = list.stream().map(v -> v.getToUserId()).collect(Collectors.toList());
        u1.addAll(u2);
        List<Account> accounts = accountService.selectByIds(u1);
        Map<Long, Account> userId2UserInfoMap = accounts.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        Map<Long, List<ProjectReview>> toUserId2PrListMap = list.stream().collect(Collectors.groupingBy(ProjectReview::getToUserId));

        List<ProjectAvgEvaluateListResp> paixuProjectIds = new ArrayList<>();
        for (Long toUserId : toUserId2PrListMap.keySet()) {
            List<ProjectReview> prOfUser = toUserId2PrListMap.get(toUserId);

            List<ProjectAvgEvaluateListResp.ProjectEvaluateResp> respList = prOfUser.stream().map(v -> {
                ProjectAvgEvaluateListResp.ProjectEvaluateResp r = new ProjectAvgEvaluateListResp.ProjectEvaluateResp();
                if (userId2UserInfoMap.containsKey(v.getFromUserId())) {
                    r.setFromUserName(userId2UserInfoMap.get(v.getFromUserId()).getNickname());
                    r.setFromUserAvatar(userId2UserInfoMap.get(v.getFromUserId()).getAvatarUrl());
                }
                r.setComment(v.getComment());
                r.setScore(v.getScore());
                return r;
            }).collect(Collectors.toList());

            //计算prOfUser中的score字段的平均值，并且四舍五入保留2位小数

            double avgScore = prOfUser.stream().mapToInt(v -> v.getScore()).average().orElse(0);
            if (avgScore > 0) {
                avgScore = Math.round(avgScore * 100) / 100.0;
            }

            ProjectAvgEvaluateListResp r = new ProjectAvgEvaluateListResp();
            r.setUserId(toUserId);
            r.setUserName(userId2UserInfoMap.get(toUserId).getNickname());
            r.setAvatar(userId2UserInfoMap.get(toUserId).getAvatarUrl());
            r.setAvgScore(avgScore);
            r.setEvaluateList(respList);
            paixuProjectIds.add(r);
        }
        return paixuProjectIds;
    }
}
