package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.req.UpdateUserInfoReq;
import com.example.entity.resp.UserAllInfo;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:57
 */
@Service
public class UserFacade {
    @Resource
    AccountService accountService;
    public UserAllInfo getUserAllInfo(Long id) {
        Account userAllInfo = accountService.selectById(id);

        UserAllInfo r = new UserAllInfo();
        BeanUtils.copyProperties(userAllInfo, r);
        return r;
    }

    public Boolean updateUserInfo(UpdateUserInfoReq req) {
        //根据id更新用户的字段
        Account account = accountService.selectById(req.getId());
        if (account == null) {
            throw new ValidationException("用户不存在");
        }
        account.setNickname(req.getNickname());
        account.setUsername(req.getUsername());
        account.setSex(req.getSex());
        account.setAvatarUrl(req.getAvatarUrl());
        account.setPhone(req.getPhone());
        account.setProvince(req.getProvince());
        account.setCity(req.getCity());
        account.setCounty(req.getCounty());
        account.setIndustryCode(req.getIndustryCode());
        return accountService.updateById(account);
    }
}
