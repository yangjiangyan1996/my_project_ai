package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.dto.ProjectMembers;
import com.example.entity.resp.MemberListResp;
import com.example.enums.ProjectEnum;
import com.example.service.AccountService;
import com.example.service.ProjectMembersService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    AccountService accountService;
    @Resource
    private ProjectMembersService projectMembersService;

    public MemberListResp memberList(Long projectId, Long userId) {
        List<ProjectMembers> members = projectMembersService.selectByProjectId(projectId);

        List<Long> userIds = members.stream().map(v -> v.getUserId()).collect(Collectors.toList());
        List<Account> userAccount = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userAccount.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        MemberListResp r = new MemberListResp();
        List<MemberListResp.MemberInfo> list = members.stream().map(v -> {
                    MemberListResp.MemberInfo memberGetResp = new MemberListResp.MemberInfo();
                    memberGetResp.setId(v.getId());
                    memberGetResp.setAvatarUrl(userId2UserInfoMap.get(v.getUserId()).getAvatarUrl());
                    memberGetResp.setNickname(userId2UserInfoMap.get(v.getUserId()).getNickname());
                    memberGetResp.setUsername(userId2UserInfoMap.get(v.getUserId()).getUsername());
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
}
