package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectMembers;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:41
 */
public interface ProjectMembersService extends IService<ProjectMembers> {
    List<ProjectMembers> selectByProjectIdAndNeRole(Long projectId, Integer notEqRoleCode);

    List<ProjectMembers> selectByUserId(Long userid);

    Page<ProjectMembers> getMyProjectMemberGroupList(Page<ProjectMembers> page, Long userId);

    List<ProjectMembers> selectByProjectId(Long projectId, Integer status);
}