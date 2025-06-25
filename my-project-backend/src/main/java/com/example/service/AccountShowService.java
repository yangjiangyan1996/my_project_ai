package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.AccountShow;

import java.util.List;

public interface AccountShowService extends IService<AccountShow> {
    // 自定义业务方法
    List<AccountShow> findByUserId(Long userId);
}