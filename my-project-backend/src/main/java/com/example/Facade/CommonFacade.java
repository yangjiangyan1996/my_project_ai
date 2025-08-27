package com.example.Facade;

import com.example.entity.dto.Account;
import com.example.entity.req.SearchUserReq;
import com.example.entity.resp.UserSearchResp;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/4 11:10
 */
@Service
public class CommonFacade {

    @Resource
    AccountService accountService;

    public Long getUserIdBySecrecyId(Long secrecyId) {
        return accountService.selectBySecrecyId(secrecyId).getId();
    }

    public List<UserSearchResp> myMemberGroups(SearchUserReq req) {
        List<Account> list = accountService.searchByReq(req);
        return list.stream().map(v -> {
            UserSearchResp memberInfo = new UserSearchResp();
            memberInfo.setId(v.getId());
            memberInfo.setUsername(v.getNickname());
            memberInfo.setAvatarUrl(v.getAvatarUrl());
            memberInfo.setSex(v.getSex());
            return memberInfo;
        }).collect(Collectors.toList());
    }
}
