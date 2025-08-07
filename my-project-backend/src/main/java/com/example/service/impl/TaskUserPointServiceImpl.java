package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskUserPoint;
import com.example.mapper.TaskUserPointMapper;
import com.example.service.TaskUserPointService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:30
 */
@Service
public class TaskUserPointServiceImpl extends ServiceImpl<TaskUserPointMapper, TaskUserPoint> implements TaskUserPointService {
    @Override
    public TaskUserPoint selectByUserId(Long userId) {
        return this.baseMapper.selectOne(new QueryWrapper<TaskUserPoint>()
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }
}
