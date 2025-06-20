package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.mapper.ProjectsMapper;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:54
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectsMapper, Projects> implements ProjectService {

    @Resource
    private ProjectsMapper projectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public Page<Projects> getHotFuyeProjects(Page<Projects> pageable, ProjectListReq req) {
        return projectMapper.selectPage(
                pageable,
                new QueryWrapper<Projects>()
                        .select("id", "name", "category", "description", "difficulty", "image_url", "created_at")
                        .eq("status", 1)
                        .eq(req.getCategory() != null, "category", req.getCategory())
                        .eq(req.getDifficulty() != null, "difficulty", req.getDifficulty())
                        .like(req.getProjectName() != null, "name", req.getProjectName())
                        .orderByDesc("created_at")
        );
    }
}
