package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TaskUserProgress;

import java.util.List;
import java.util.Optional;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/5 11:58
 */
public interface TaskUserProgressService  extends IService<TaskUserProgress> {
    Optional<TaskUserProgress> findByUserIdAndTaskId(Long userId, Long taskId);

    List<TaskUserProgress> selectByUserId(Long userId, Integer status);
}
