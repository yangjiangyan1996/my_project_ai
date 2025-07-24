package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.ProjectMembers;
import com.example.entity.dto.QuanBars;
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
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<QuanBars> selectByFirstCategory(Long categoryId) {
        return this.baseMapper.selectList(
                new QueryWrapper<QuanBars>()
                        .eq(categoryId!=null, "first_category", categoryId)
                        .eq("is_deleted", 0)
        );
    }
}