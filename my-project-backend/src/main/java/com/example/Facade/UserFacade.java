package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.resp.UserAllInfo;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
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
}
