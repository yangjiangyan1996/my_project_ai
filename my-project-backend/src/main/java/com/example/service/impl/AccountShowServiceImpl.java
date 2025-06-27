package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.AccountShow;
import com.example.enums.UserEnums;
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

    @Override
    public Page<AccountShow> getProjectShowList(Page<AccountShow> page) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<AccountShow>()
                        .eq("status", UserEnums.AccountShowEnum.Yes.getCode())
                        .orderByDesc("created_at")
        );
    }

    @Override
    public List<AccountShow> selectByUserIds(List<Long> userIds) {
        return baseMapper.selectList(
                new QueryWrapper<AccountShow>()
                        .eq("status", UserEnums.AccountShowEnum.Yes.getCode())
                        .in("user_id", userIds)
        );
    }
}