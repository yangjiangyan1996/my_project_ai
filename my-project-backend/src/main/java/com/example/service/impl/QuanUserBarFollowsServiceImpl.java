package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanUserBarFollows;
import com.example.entity.req.BarMyFavoriteReq;
import com.example.mapper.QuanUserBarFollowsMapper;
import com.example.service.QuanUserBarFollowsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 16:00
 */
@Service
public class QuanUserBarFollowsServiceImpl extends ServiceImpl<QuanUserBarFollowsMapper, QuanUserBarFollows> implements QuanUserBarFollowsService {
    @Override
    public QuanUserBarFollows selectByBarIdAndUserId(Long barId, Long userId) {
        return this.baseMapper.selectOne(new QueryWrapper<QuanUserBarFollows>()
                .eq("bar_id", barId)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public Boolean insert(Long barId, Long userId) {
        QuanUserBarFollows q = new QuanUserBarFollows();
        q.setCreatedBy(userId);
        q.setCreatedAt(new Date());
        q.setModifiedAt(new Date());
        q.setModifiedBy(userId);
        q.setBarId(barId);
        q.setUserId(userId);
        q.setIsDeleted(0);
        return this.baseMapper.insert(q) >0;
    }

    @Override
    public Long selectCountByBarId(Long barId) {
        return this.baseMapper.selectCount(new QueryWrapper<QuanUserBarFollows>()
                .eq("bar_id", barId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<QuanUserBarFollows> selectByBarIdsAndUserId(List<Long> barList, Long userId) {
        return this.baseMapper.selectList(new QueryWrapper<QuanUserBarFollows>()
                .in("bar_id", barList)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public Page<QuanUserBarFollows> getMyFavoriteBar(Page<QuanUserBarFollows> of, BarMyFavoriteReq req) {
        return this.baseMapper.selectPage(of,
                new QueryWrapper<QuanUserBarFollows>()
                        .eq("user_id", req.getUserId())
                        .eq("is_deleted", 0)
                        .orderByDesc("created_at")
        );
    }
}
