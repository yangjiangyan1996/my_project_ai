package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskUserProgress;
import com.example.entity.query.TaskProgressQuery;
import com.example.mapper.TaskUserProgressMapper;
import com.example.service.TaskUserProgressService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.comlast_progress_time
 * @Date 2025/8/5 11:58
 */
@Service
public class TaskUserProgressServiceImpl extends ServiceImpl<TaskUserProgressMapper, TaskUserProgress> implements TaskUserProgressService {
    @Override
    public Optional<TaskUserProgress> findByUserIdAndTaskId(TaskProgressQuery query) {
        return Optional.ofNullable(selectByQuery(query));
    }

    @Override
    public List<TaskUserProgress> selectByUserId(Long userId, Integer status) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(new QueryWrapper<TaskUserProgress>()
                .eq("user_id", userId)
                .eq(status != null, "status", status)
                .eq("is_deleted", 0));
    }


    @Override
    public TaskUserProgress selectByQuery(TaskProgressQuery query) {
        return this.baseMapper.selectOne(new QueryWrapper<TaskUserProgress>()
                .eq(query.getUserId() != null,"user_id", query.getUserId())
                .eq(query.getTaskId() != null,"task_id", query.getTaskId())
                .ge(query.getStartTime() != null,"last_progress_time", query.getStartTime())
                .le(query.getEndTime() != null,"last_progress_time", query.getEndTime())
                .eq("is_deleted", 0));
    }
}
