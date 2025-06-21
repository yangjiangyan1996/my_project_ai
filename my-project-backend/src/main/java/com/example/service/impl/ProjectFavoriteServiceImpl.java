package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectFavorite;
import com.example.mapper.ProjectFavoriteMapper;
import com.example.service.ProjectFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class ProjectFavoriteServiceImpl extends ServiceImpl<ProjectFavoriteMapper, ProjectFavorite> implements ProjectFavoriteService {}