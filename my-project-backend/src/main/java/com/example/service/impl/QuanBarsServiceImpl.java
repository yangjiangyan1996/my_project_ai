package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectMembers;
import com.example.entity.dto.QuanBars;
import com.example.entity.req.BarMyFavoriteReq;
import com.example.enums.QuanEnum;
import com.example.enums.TieEnum;
import com.example.mapper.QuanBarsMapper;
import com.example.service.QuanBarsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuanBarsServiceImpl extends ServiceImpl<QuanBarsMapper,QuanBars > implements QuanBarsService {
    @Override
    public List<QuanBars> selectByName(String name) {
        return this.baseMapper.selectList(
                new QueryWrapper<QuanBars>()
                        .eq(name!=null, "name", name)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
        );
    }


    @Override
    public List<QuanBars> selectByIds(List<Long> barIds) {
        return this.baseMapper.selectList(
                new QueryWrapper<QuanBars>()
                        .in("id", barIds)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<QuanBars> selectByTieIds(List<Long> barIds) {
        return this.baseMapper.selectList(
                new QueryWrapper<QuanBars>()
                        .in("id", barIds)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<QuanBars> selectByFirstCategory(Integer categoryId) {
        return this.baseMapper.selectList(
                new QueryWrapper<QuanBars>()
                        .eq(categoryId!=null, "first_category", categoryId)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public Page<QuanBars> selectPageByFirstCategory(Page<QuanBars> page,Integer categoryId) {
        return this.baseMapper.selectPage(page,
                new QueryWrapper<QuanBars>()
                        .eq(categoryId!=null, "first_category", categoryId)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
                        .orderByDesc("follower_count")
        );
    }

    @Override
    public Page<QuanBars> selectFollowBars(Page<QuanBars> page) {
        return this.baseMapper.selectPage(page,
                new QueryWrapper<QuanBars>()
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("is_deleted", 0)
                        .orderByDesc("follower_count")
        );
    }


    @Override
    public Page<QuanBars> selectBySecondCategory(Page<QuanBars> page, Integer secondCategory) {
        return this.baseMapper.selectPage(page,
                new QueryWrapper<QuanBars>()
                        .eq("is_deleted", 0)
                        .eq("status", QuanEnum.BarStatusEnums.AUDIT_PASS.getCode())
                        .eq("second_category", secondCategory)
                        .orderByDesc("follower_count")
        );
    }

    @Override
    public QuanBars selectById(Long barId) {
        if (barId == null) {
            return null;
        }
        return this.baseMapper.selectById(barId);
    }
}