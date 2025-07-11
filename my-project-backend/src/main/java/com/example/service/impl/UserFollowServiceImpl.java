package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.UserFollow;
import com.example.mapper.UserFollowMapper;
import com.example.service.UserFollowService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 16:25
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {
    @Override
    public Boolean removeUserByUserId(Long userId, Long followeeId) {
        return remove(new QueryWrapper<UserFollow>().eq("follower_id", userId).eq("followee_id", followeeId));
    }

    @Override
    public UserFollow selectUserByUserId(Long followerId, Long followeeId) {
        return getOne(new QueryWrapper<UserFollow>().eq("follower_id", followerId)
                .eq("followee_id", followeeId)
                .eq("is_deleted", 0));
    }

    @Override
    public Boolean updateIsMutual(Long followerId, Long followeeId, Integer code) {
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        return update(userFollow, new QueryWrapper<UserFollow>()
                .eq("follower_id", followerId)
                .eq("followee_id", followeeId)
                .eq("is_deleted", 0));
    }

    @Override
    public Boolean selectByUserIdAndFollowedId(Long followerId, Long followeeId) {
        return this.baseMapper.exists(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId)
                .eq(UserFollow::getIsDeleted, 0));
    }

    @Override
    public List<UserFollow> selectByFollowerId(Long followerId) {
        return list(new QueryWrapper<UserFollow>().eq("follower_id", followerId)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<UserFollow> selectPageByFollowerId(Page<UserFollow> page, Long userId) {
        return page(page, new QueryWrapper<UserFollow>().eq("follower_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<UserFollow> selectPageByFolloweeId(Page<UserFollow> page, Long followeeId) {
        return page(page, new QueryWrapper<UserFollow>().eq("followee_id", followeeId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<UserFollow> selectByFolloweeId(Long followeeId) {
        return list(new QueryWrapper<UserFollow>()
                .eq("followee_id", followeeId)
                .eq("is_deleted", 0));
    }
}
