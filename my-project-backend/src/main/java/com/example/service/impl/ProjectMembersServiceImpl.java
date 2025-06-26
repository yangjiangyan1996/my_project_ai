package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectMembers;
import com.example.mapper.ProjectMembersMapper;
import com.example.service.ProjectMembersService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 11:41
 */
@Service
public class ProjectMembersServiceImpl extends ServiceImpl<ProjectMembersMapper, ProjectMembers> implements ProjectMembersService {
}
