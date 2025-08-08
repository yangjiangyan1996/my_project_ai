package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskUserBadge;
import com.example.mapper.TaskUserBadgeMapper;
import com.example.service.TaskUserBadgeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 20:07
 */
@Service
public class TaskUserBadgeServiceImpl extends ServiceImpl<TaskUserBadgeMapper, TaskUserBadge> implements TaskUserBadgeService {
    @Override
    public List<TaskUserBadge> selectByUserId(Long userId) {
        return this.baseMapper.selectList(new QueryWrapper<TaskUserBadge>()
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }
}
