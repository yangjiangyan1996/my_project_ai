package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProjectReview;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/18 10:06
 */
public interface ProjectReviewService extends IService<ProjectReview> {
    List<ProjectReview> selectByProjectIdAndFromUserId(Long projectId, Long userId);

    ProjectReview selectByProjectIdAndFromUserIdAndToUserId(Long projectId, Long fromUserId, Long toUserId);
}
