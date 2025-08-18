package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.QuanBars;

import java.util.List;

public interface QuanBarsService extends IService<QuanBars> {
    List<QuanBars> selectByName(String name);

    List<QuanBars> selectByFirstCategory(Integer categoryId);

    QuanBars selectById(Long barId);

    Page<QuanBars> selectBySecondCategory(Page<QuanBars> page, Integer secondCategory);

    Page<QuanBars> selectPageByFirstCategory(Page<QuanBars> page, Integer firstCategory);

    Page<QuanBars> selectFollowBars(Page<QuanBars> of);

    List<QuanBars> selectByTieIds(List<Long> barIds);

    List<QuanBars> selectByIds(List<Long> barIds);
}