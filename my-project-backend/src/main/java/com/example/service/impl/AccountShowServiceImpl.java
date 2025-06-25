package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.AccountShow;
import com.example.mapper.AccountShowMapper;
import com.example.service.AccountShowService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountShowServiceImpl extends ServiceImpl<AccountShowMapper, AccountShow> implements AccountShowService {

    @Override
    public AccountShow getByUserId(Long userId) {
        return query().eq("user_id", userId).one();
    }
}