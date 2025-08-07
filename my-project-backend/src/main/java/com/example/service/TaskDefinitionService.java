package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TaskDefinition;
import com.example.entity.req.AchievementMyPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/5 11:58
 */
public interface TaskDefinitionService  {
    List<TaskDefinition> findByCategoryAndStatus(List<String> category, int status);

    Page<TaskDefinition> myAchievementPageList(Page<TaskDefinition> page, AchievementMyPageReq req);

    List<TaskDefinition> selectByIds(List<Long> taskIds);

    TaskDefinition selectById(Long taskId);
}
