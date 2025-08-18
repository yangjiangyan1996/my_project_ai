package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TaskUserPoint;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:30
 */
public interface TaskUserPointService extends IService<TaskUserPoint> {
    TaskUserPoint selectByUserId(Long userId);
}
