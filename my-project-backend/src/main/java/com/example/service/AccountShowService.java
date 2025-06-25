package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.AccountShow;

import java.util.List;

public interface AccountShowService extends IService<AccountShow> {

    AccountShow getByUserId(Long userId);
}