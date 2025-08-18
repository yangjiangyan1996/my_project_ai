package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TaskBadge;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:29
 */
public interface TaskBadgeService extends IService<TaskBadge> {
    TaskBadge selectByTaskId(Long taskId);

    List<TaskBadge> selectByIds(List<Long> badgeIds);
}
