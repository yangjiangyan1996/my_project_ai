package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Projects;
import com.example.entity.dto.TaskDefinition;
import com.example.entity.req.AchievementMyPageReq;
import com.example.enums.AchievementEnums;
import com.example.enums.ProjectEnum;
import com.example.mapper.TaskDefinitionMapper;
import com.example.service.TaskDefinitionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/5 11:59
 */
@Service
public class TaskDefinitionServiceImpl extends ServiceImpl<TaskDefinitionMapper, TaskDefinition> implements TaskDefinitionService {
    @Override
    public List<TaskDefinition> findByCategoryAndStatus(String category, int status) {
        return null;
    }

    @Override
    public Page<TaskDefinition> myAchievementPageList(Page<TaskDefinition> page, AchievementMyPageReq req) {
        return this.baseMapper.selectPage(
                page,
                new QueryWrapper<TaskDefinition>()
                        .gt("status", AchievementEnums.TaskStatus.ENABLED)
                        .orderByDesc("sort_order")
        );
    }
}
