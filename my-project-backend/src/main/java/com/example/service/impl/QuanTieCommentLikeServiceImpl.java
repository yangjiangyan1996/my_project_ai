package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectCommentLike;
import com.example.entity.dto.QuanTieCommentLike;
import com.example.mapper.QuanTieCommentLikeMapper;
import com.example.service.QuanTieCommentLikeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:28
 */
@Service
public class QuanTieCommentLikeServiceImpl  extends ServiceImpl<QuanTieCommentLikeMapper, QuanTieCommentLike> implements QuanTieCommentLikeService {
    @Override
    public List<QuanTieCommentLike> selectByTieId(Long tieId) {
        return baseMapper.selectList(new QueryWrapper<QuanTieCommentLike>().eq("tie_id", tieId).eq("is_deleted", 0));
    }

    @Override
    public QuanTieCommentLike selectByCommentIdAndUserId(Long commentId, Long tieId, Long userId) {
        return baseMapper.selectOne(new QueryWrapper<QuanTieCommentLike>()
                .eq("comment_id", commentId)
                .eq("tie_id", tieId).eq("user_id", userId)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public Boolean insert(Long commentId, Long tieId, Long userId) {
        return save(new QuanTieCommentLike(null, commentId, userId,tieId));

    }
}
