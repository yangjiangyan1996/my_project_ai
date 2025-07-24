package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanBars;
import com.example.mapper.QuanBarsMapper;
import com.example.service.QuanBarsService;
import org.springframework.stereotype.Service;

@Service
public class QuanBarsServiceImpl extends ServiceImpl<QuanBarsMapper, QuanBars> implements QuanBarsService {
}