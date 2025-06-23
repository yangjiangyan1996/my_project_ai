package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Projects;
import com.example.entity.dto.UserFollow;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 16:25
 */
public interface UserFollowService extends IService<UserFollow> {
    Boolean removeUserByUserId(Long followerId, Long followeeId);

    UserFollow selectUserByUserId(Long followerId, Long followeeId);

    Boolean updateIsMutual(Long followeeId, Long userId, Integer code);
}
