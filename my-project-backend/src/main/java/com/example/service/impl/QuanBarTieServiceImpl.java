package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanBarTie;
import com.example.entity.req.QuanTieListPageReq;
import com.example.enums.QuanEnum;
import com.example.mapper.QuanBarTieMapper;
import com.example.service.QuanBarTieService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:35
 */
@Service
public class QuanBarTieServiceImpl extends ServiceImpl<QuanBarTieMapper, QuanBarTie> implements QuanBarTieService {
    @Override
    public Page<QuanBarTie> getTiePageOfBar(Page<QuanBarTie> page, QuanTieListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<QuanBarTie>()
                        .eq(req.getBarId() != null, "bar_id", req.getBarId())
                        .eq("is_deleted", 0)
                        .like(req.getKeyword() != null, "title", req.getKeyword())
                        .eq("status", QuanEnum.TieStatusEnums.NORMAL.getCode())
                        .orderByDesc("created_at")
        );
    }

    @Override
    public QuanBarTie selectByTieId(Long tieId) {
        return baseMapper.selectOne(new QueryWrapper<QuanBarTie>()
                .eq("id", tieId)
                .eq("is_deleted", 0)
        );
    }

    @Override
    public Long selectTieCountByBarId(Long barId) {
        return baseMapper.selectCount(new QueryWrapper<QuanBarTie>()
                .eq("bar_id", barId)
                .eq("is_deleted", 0)
                .eq("status", QuanEnum.TieStatusEnums.NORMAL.getCode()));
    }

    @Override
    public List<QuanBarTie> selectByTieIds(List<Long> tieids) {
        if (CollectionUtils.isEmpty(tieids)) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(new QueryWrapper<QuanBarTie>()
                .in("id", tieids)
                .eq("is_deleted", 0)
                .eq("status", QuanEnum.TieStatusEnums.NORMAL.getCode()));
    }


}
