package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.*;
import com.example.entity.req.AddMemberByManagerReq;
import com.example.entity.req.MyMemberGroupsReq;
import com.example.entity.req.RemoveMemberReq;
import com.example.entity.resp.MemberListResp;
import com.example.entity.resp.MyMemberGroupsResp;
import com.example.enums.ProjectEnum;
import com.example.enums.UserEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/3 15:08
 */
@Service
public class ProjectMemberFacade {
    @Resource
    ProjectFacade projectFacade;
    @Resource
    AccountService accountService;
    @Resource
    ProjectApplicationsService projectApplicationsService;
    @Resource
    ProjectService projectService;
    @Resource
    MessageFacade messageFacade;
    @Resource
    ChatConversationService chatConversationService;
    @Resource
    ChatConversationMemberService chatConversationMemberService;
    @Resource
    private ProjectMembersService projectMembersService;

    public Page<MyMemberGroupsResp> myMemberGroups(MyMemberGroupsReq req, Long userId) {
        Page<ProjectMembers> myProjects = projectMembersService.getMyProjectMemberGroupList(Page.of(req.getPage() - 1, req.getSize()), userId);
        if (myProjects.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }
        List<Long> projectIds = myProjects.getRecords().stream().map(v -> v.getProjectId()).collect(Collectors.toList());

        List<Projects> projectList = projectService.selectByProjectIds(projectIds);
        Map<Long, Projects> projectId2ProjectsMap = projectList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v, (l1, l2) -> l2));

        List<MyMemberGroupsResp> collect = myProjects.getRecords().stream().map(v -> {
            MyMemberGroupsResp r = new MyMemberGroupsResp();
            r.setId(v.getId());
            r.setProjectId(v.getProjectId());
            r.setRoleOfMemberGroup(ProjectEnum.ProjectMemberRoleEnum.getByCode(v.getRole()).getName());
            r.setCreatedAt(v.getCreatedAt());
            r.setName(projectId2ProjectsMap.getOrDefault(v.getProjectId(), new Projects()).getName());
            return r;
        }).collect(Collectors.toList());

        Page<MyMemberGroupsResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(myProjects.getTotal());
        result.setRecords(collect);
        return result;
    }


    public MemberListResp memberList(Long projectId, Long userId) {
        List<ProjectMembers> members = projectMembersService.selectByProjectId(projectId, null);

        List<Long> userIds = members.stream().map(v -> v.getUserId()).collect(Collectors.toList());
        List<Account> userAccount = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userAccount.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        MemberListResp r = new MemberListResp();
        List<MemberListResp.MemberInfo> list = members.stream().map(v -> {
                    MemberListResp.MemberInfo memberGetResp = new MemberListResp.MemberInfo();
                    memberGetResp.setId(v.getId());
                    memberGetResp.setUserId(v.getUserId());
                    memberGetResp.setSexName(UserEnums.SexEnum.getByCode(userId2UserInfoMap.get(v.getUserId()).getSex()).getName());
                    memberGetResp.setAvatarUrl(userId2UserInfoMap.get(v.getUserId()).getAvatarUrl());
                    memberGetResp.setNickname(userId2UserInfoMap.get(v.getUserId()).getNickname());
                    memberGetResp.setStatusName(ProjectEnum.MemberStatusEnum.getByCode(v.getStatus()).getName());
                    memberGetResp.setEmail(userId2UserInfoMap.get(v.getUserId()).getEmail());
                    memberGetResp.setProvince(userId2UserInfoMap.get(v.getUserId()).getProvince());
                    memberGetResp.setCity(userId2UserInfoMap.get(v.getUserId()).getCity());
                    memberGetResp.setCreatedAt(v.getJoinTime());
                    memberGetResp.setRoleOfMemberGroupCode(v.getRole());
                    memberGetResp.setRoleOfMemberGroup(ProjectEnum.ProjectMemberRoleEnum.getByCode(v.getRole()).getName());
                    return memberGetResp;
                }).sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());

        r.setMembers(list);

        ProjectMembers projectMembers = members.stream().filter(v -> v.getUserId().equals(userId)).findFirst().orElse(null);
        if (projectMembers != null) {
            r.setCurrentUserRole(ProjectEnum.ProjectMemberRoleEnum.getByCode(projectMembers.getRole()).getName());
        }
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMember(RemoveMemberReq req, Long userId) {
        List<ProjectMembers> pm = projectMembersService.selectByProjectId(req.getProjectId(), ProjectEnum.MemberStatusEnum.IN.getCode());
        if (CollectionUtils.isEmpty(pm)) {
            return false;
        }
        List<Long> userIdsOfAmdin = pm.stream().filter(v -> v.getRole().equals(ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode())).map(v -> v.getUserId()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdsOfAmdin)) {
            return false;
        }
        if (!userIdsOfAmdin.contains(userId)) {
            throw new ValidationException("无权操作");
        }

        List<Long> ids = pm.stream().filter(v -> v.getRole().equals(ProjectEnum.ProjectMemberRoleEnum.ADMIN.getCode())).map(v -> v.getId()).distinct().collect(Collectors.toList());
        if (ids.contains(req.getMemberId())) {
            throw new ValidationException("不能移除管理员");
        }
        ProjectMembers projectMembers = pm.stream().filter(v -> v.getId().equals(req.getMemberId())).findFirst().orElse(null);
        if (projectMembers == null) {
            throw new ValidationException("成员不存在");
        }
        projectMembers.setExitTime(new Date());
        projectMembers.setExitMessage(req.getExitMessage());
        projectMembers.setStatus(req.getStatus());
        boolean b = projectMembersService.updateById(projectMembers);
        if (!b) {
            throw new ValidationException("更新成员失败");
        }
        boolean remove = projectApplicationsService.remove(new QueryWrapper<ProjectApplications>().eq("project_id", req.getProjectId()).eq("user_id", projectMembers.getUserId()));
        if (remove && Boolean.FALSE.equals(projectFacade.projectFull(req.getProjectId()))){
            projectService.updateStatus(req.getProjectId(), ProjectEnum.ProjectStatusEnum.PUBLISHING.getCode(), null, userId);
        }

        return b;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean addMemberByManager(AddMemberByManagerReq req, Long userId) {
        ProjectMembers p = projectMembersService.selectByProjectIdAndUserId(req.getProjectId(), req.getUserId());

        Boolean addResult = false;
        if (p != null) {
            if (Objects.equals(p.getStatus(), ProjectEnum.MemberStatusEnum.IN.getCode())) {
                throw new ValidationException("用户已加入");
            } else {
                ProjectMembers e = new ProjectMembers();
                e.setId(p.getId());
                e.setStatus(ProjectEnum.MemberStatusEnum.IN.getCode());
                e.setRole(ProjectEnum.ProjectMemberRoleEnum.getByCode(req.getRole()).getCode());
                e.setModifiedBy(userId);
                e.setModifiedAt(new Date());
                addResult = projectMembersService.updateById(e);
            }
        } else {
            ProjectMembers e = new ProjectMembers();
            e.setProjectId(req.getProjectId());
            e.setUserId(req.getUserId());
            e.setJoinTime(new Date());
            e.setRole(ProjectEnum.ProjectMemberRoleEnum.getByCode(req.getRole()).getCode());
            e.setStatus(ProjectEnum.MemberStatusEnum.IN.getCode());
            e.setCreatedAt(new Date());
            e.setCreatedBy(userId);
            e.setModifiedBy(userId);
            e.setModifiedAt(new Date());
            addResult = projectMembersService.save(e);
        }
        if (addResult) {
            //加入到聊天中
            ChatConversation cc = chatConversationService.selectByProjectId(req.getProjectId());
            if (cc != null) {
                ChatConversationMember ccm = new ChatConversationMember();
                ccm.setConversationId(cc.getId());
                ccm.setUserId(req.getUserId());
                ccm.setJoinedAt(new Date());
                chatConversationMemberService.save(ccm);
            }
        }
        return addResult;
    }
}
