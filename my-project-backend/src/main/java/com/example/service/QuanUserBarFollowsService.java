package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.QuanUserBarFollows;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 16:00
 */
public interface QuanUserBarFollowsService extends IService<QuanUserBarFollows> {
    QuanUserBarFollows selectByBarIdAndUserId(Long barId, Long userId);

    Boolean insert(Long barId, Long userId);

    Long selectCountByBarId(Long barId);
}
