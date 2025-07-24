package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.QuanBars;

import java.util.List;

public interface QuanBarsService extends IService<QuanBars> {
    List<QuanBars> selectByName(String name);
}