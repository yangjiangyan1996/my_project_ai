package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectFavorite;
import com.example.entity.dto.QuanTieFavorite;
import com.example.mapper.QuanTieFavoriteMapper;
import com.example.service.QuanTieFavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/28 17:13
 */
@Service
public class QuanTieFavoriteServiceImpl extends ServiceImpl<QuanTieFavoriteMapper, QuanTieFavorite> implements QuanTieFavoriteService {
    @Override
    public Boolean insertOne(QuanTieFavorite f) {
        return save(f);
    }

    @Override
    public Boolean removeOne(Long tieId, Long userId) {
        return this.baseMapper.delete(new QueryWrapper<QuanTieFavorite>().eq("tie_id", tieId).eq("user_id", userId)) > 0;
    }

    @Override
    public boolean selectByTieIdAndUserId(Long tieId, Long userId) {
        Long count = this.baseMapper.selectCount(new QueryWrapper<QuanTieFavorite>().eq("tie_id", tieId).eq("user_id", userId));
        return count > 0;
    }

    @Override
    public List<QuanTieFavorite> selectByTieIdsAndUserId(List<Long> tieIds, Long userId) {
        return this.baseMapper.selectList(new QueryWrapper<QuanTieFavorite>()
                .in("tie_id", tieIds)
                .eq("user_id", userId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<QuanTieFavorite> selectByTieId(Long tieId) {
        return this.baseMapper.selectList(new QueryWrapper<QuanTieFavorite>()
                .eq("tie_id", tieId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<QuanTieFavorite> selectByTieIds(List<Long> tieIds) {
        return this.baseMapper.selectList(new QueryWrapper<QuanTieFavorite>()
                .in("tie_id", tieIds)
                .eq("is_deleted", 0));
    }
}
