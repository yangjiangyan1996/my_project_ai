package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectLike;
import com.example.mapper.ProjectLikeMapper;
import com.example.service.ProjectLikeService;
import org.springframework.stereotype.Service;

@Service
public class ProjectLikeServiceImpl extends ServiceImpl<ProjectLikeMapper, ProjectLike> implements ProjectLikeService {
    // 自动继承MyBatis-Plus的save/remove/update/get等基础CRUD方法
}