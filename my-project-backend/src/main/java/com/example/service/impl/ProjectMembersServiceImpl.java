package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectMembers;
import com.example.entity.dto.Projects;
import com.example.enums.ProjectEnum;
import com.example.mapper.ProjectMembersMapper;
import com.example.service.ProjectMembersService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:41
 */
@Service
public class ProjectMembersServiceImpl extends ServiceImpl<ProjectMembersMapper, ProjectMembers> implements ProjectMembersService {
    @Override
    public List<ProjectMembers> selectByProjectIdAndNeRole(Long projectId, Integer notEqRoleCode) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectMembers>().eq("project_id", projectId)
                .ne("role", notEqRoleCode)
                        .eq("status", ProjectEnum.MemberStatusEnum.IN.getCode())
                .eq("is_deleted", 0));
    }

    @Override
    public List<ProjectMembers> selectByUserId(Long userid) {
        return this.baseMapper.selectList(new QueryWrapper<ProjectMembers>().eq("user_id", userid)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<ProjectMembers> getMyProjectMemberGroupList(Page<ProjectMembers> page, Long userId) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<ProjectMembers>()
                        .eq(userId != null, "user_id", userId)
                        .eq("is_deleted", 0)
                        .eq("status", ProjectEnum.MemberStatusEnum.IN.getCode())
                        .orderByDesc("join_time")
        );
    }

    @Override
    public List<ProjectMembers> selectByProjectId(Long projectId, Integer status) {
        return this.baseMapper.selectList(
                new QueryWrapper<ProjectMembers>()
                        .eq("project_id", projectId)
                        .eq(status != null, "status", status)
                        .eq("is_deleted", 0)
        );
    }
}
