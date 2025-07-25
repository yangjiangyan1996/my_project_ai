package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.Account;
import com.example.entity.dto.ProjectMembers;
import com.example.entity.dto.Projects;
import com.example.entity.dto.QuanBarTie;
import com.example.entity.req.QuanTieCreateReq;
import com.example.entity.req.QuanTieListPageReq;
import com.example.entity.resp.MyMemberGroupsResp;
import com.example.entity.resp.QuanTieListPageResp;
import com.example.enums.ProjectEnum;
import com.example.enums.QuanEnum;
import com.example.service.AccountService;
import com.example.service.QuanBarTieService;
import com.example.utils.DateUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:42
 */
@Service
public class TieFacade {
    @Resource
    AccountService accountService;
    @Resource
    private QuanBarTieService quanBarTieService;

    public Boolean createTie(QuanTieCreateReq req, Long userId) {
        QuanBarTie e = new QuanBarTie();
        e.setTitle(req.getTitle());
        e.setContent(req.getContent());
        e.setAvatar(req.getAvatar());
        e.setStatus(QuanEnum.TieStatusEnums.NORMAL.getCode());
        e.setBarId(req.getBarId());
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        return quanBarTieService.save(e);
    }

    public Page<QuanTieListPageResp> getTiePageOfBar(QuanTieListPageReq req) {
        Page<QuanBarTie> page = quanBarTieService.getTiePageOfBar(Page.of(req.getPage(), req.getSize()),req);
        if (page.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<Long> userIds = page.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<QuanTieListPageResp> collect = page.getRecords().stream().map(v -> {
            QuanTieListPageResp r = new QuanTieListPageResp();
            r.setId(v.getId());
            r.setTitle(v.getTitle());
            r.setAvatar(v.getAvatar());
            r.setCreatedName(userId2UserInfoMap.get(v.getCreatedBy()).getNickname());
            r.setCreatedTime(DateUtils.date2Str(v.getCreatedAt(), "yyyy-MM-dd HH:mm"));
            return r;
        }).collect(Collectors.toList());

        Page<QuanTieListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(collect);
        return result;
    }
}
