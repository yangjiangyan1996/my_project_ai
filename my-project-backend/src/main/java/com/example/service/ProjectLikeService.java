package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectLike;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 17:55
 */
public interface ProjectLikeService extends IService<ProjectLike> {
    Boolean likeProject(Long projectId, Long userId, Boolean liked);
}
