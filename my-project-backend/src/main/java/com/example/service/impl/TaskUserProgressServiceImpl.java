package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TaskUserProgress;
import com.example.mapper.TaskUserProgressMapper;
import com.example.service.TaskUserProgressService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/5 11:58
 */
@Service
public class TaskUserProgressServiceImpl extends ServiceImpl<TaskUserProgressMapper, TaskUserProgress> implements TaskUserProgressService {
    @Override
    public Optional<TaskUserProgress> findByUserIdAndTaskId(Long userId, Long taskId) {
        return Optional.empty();
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
}
