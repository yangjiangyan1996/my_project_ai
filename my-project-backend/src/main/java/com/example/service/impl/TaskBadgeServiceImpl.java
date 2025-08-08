package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskBadge;
import com.example.mapper.TaskBadgeMapper;
import com.example.service.TaskBadgeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:31
 */
@Service
public class TaskBadgeServiceImpl extends ServiceImpl<TaskBadgeMapper, TaskBadge> implements TaskBadgeService {
    @Override
    public List<TaskBadge> selectByIds(List<Long> badgeIds) {
        return this.baseMapper.selectList(
                new QueryWrapper<TaskBadge>()
                        .in("id", badgeIds)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public TaskBadge selectByTaskId(Long taskId) {
        return this.baseMapper.selectOne(new QueryWrapper<TaskBadge>().eq("task_id", taskId).eq("is_deleted", 0));
    }
}
