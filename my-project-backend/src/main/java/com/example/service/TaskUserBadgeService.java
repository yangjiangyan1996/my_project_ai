package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TaskUserBadge;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 20:06
 */
@Service
public interface TaskUserBadgeService extends IService<TaskUserBadge> {
    List<TaskUserBadge> selectByUserId(Long userId);
}
