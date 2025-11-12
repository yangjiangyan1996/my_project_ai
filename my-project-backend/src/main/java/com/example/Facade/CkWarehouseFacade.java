package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.WareHouseResp;
import com.example.entity.dto.Account;
import com.example.service.AccountService;
import com.example.service.CkWareHouseService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:25
 */
@Service
public class CkWarehouseFacade {
    @Resource
    AccountService accountService;
    @Resource
    CkWareHouseService wareHouseService;

    public Boolean create(WareHourseCreateReq req) {
        List<Warehouse> list = wareHouseService.selectByCodeOrName(req.getCode(), req.getName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            throw new ValidationException("仓库编号和名称已存在");
        }
        Warehouse save = new Warehouse();
        BeanUtils.copyProperties(req, save);

        save.setCreatedAt(new Date());
        save.setCreatedBy(req.getUserId());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return wareHouseService.save(save);
    }

    public Page<WareHouseResp> listOfWareHouse(Page<Warehouse> page, WareHouseListPageReq req) {
        Page<Warehouse> list = wareHouseService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<Long> userIds = list.getRecords().stream().map(v -> v.getManagerId()).collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userInfoMap = userInfoList.stream().collect(Collectors.toMap(Account::getId, v -> v));
        List<WareHouseResp> collect = list.getRecords().stream().map(v -> {
            WareHouseResp p = new WareHouseResp();
            BeanUtils.copyProperties(v, p);

            p.setManagerId(v.getManagerId());
            if (userInfoMap != null && userInfoMap.containsKey(v.getManagerId()) ) {
                p.setManagerName(userInfoMap.get(v.getManagerId()).getNickname());
                p.setManagerAvatar(userInfoMap.get(v.getManagerId()).getAvatarUrl());
            }

            return p;
        }).collect(Collectors.toList());

        Page<WareHouseResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean update(WareHourseCreateReq req) {
        Warehouse wh = wareHouseService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }
        List<Warehouse> list = wareHouseService.selectByCodeOrName(req.getCode(), req.getName(), req.getTenantId());
        if (!CollectionUtils.isEmpty(list)) {
            for (Warehouse warehouse : list) {
                if (!warehouse.getId().equals(req.getId())) {
                    throw new ValidationException("仓库编号或名称已存在");
                }
            }
        }
        Warehouse save = new Warehouse();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return wareHouseService.updateById(save);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefault(WareHourseSetDefaultReq req) {
        if (req.getId() == null) {
            throw new ValidationException("仓库ID不能为空");
        }
        //将租户是req.getTenantId下，ID是req.getId的仓库设为默认仓库，其他全部设置为否
        Warehouse warehouse = new Warehouse();
        warehouse.setModifiedAt(new Date());
        warehouse.setModifiedBy(req.getUserId());

        warehouse.setDefaultWareHouse(0);
        wareHouseService.update(warehouse, new QueryWrapper<Warehouse>().eq("tenant_id", req.getTenantId()));
        warehouse.setDefaultWareHouse(1);
        return wareHouseService.update(warehouse, new QueryWrapper<Warehouse>().eq("id", req.getId()));
    }

    public Boolean updateStatus(WareHourseUpdateStatusReq req) {
        Warehouse wh = wareHouseService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }

        if (wh.getDefaultWareHouse() == 1 && req.getStatus() == 0) {
            throw new ValidationException("默认仓库不能禁用");
        }

        Warehouse save = new Warehouse();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return wareHouseService.updateById(save);
    }

    public Boolean delete(WareHouseDeleteReq req) {
        Warehouse wh = wareHouseService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("仓库不存在");
        }
        if (wh.getDefaultWareHouse() == 1) {
            throw new ValidationException("默认仓库不能删除");
        }
        Warehouse save = new Warehouse();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return wareHouseService.updateById(save);
    }

    public List<WareHouseResp> list(UserInfo user) {
        List<Warehouse> list = wareHouseService.listWareHouse(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = list.stream().map(v -> v.getManagerId()).collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userInfoMap = userInfoList.stream().collect(Collectors.toMap(Account::getId, v -> v));
        return list.stream().map(v -> {
            WareHouseResp p = new WareHouseResp();
            BeanUtils.copyProperties(v, p);

            p.setManagerId(v.getManagerId());
            if(userInfoMap != null && userInfoMap.containsKey(v.getManagerId())) {
                p.setManagerName(userInfoMap.get(v.getManagerId()).getNickname());
                p.setManagerAvatar(userInfoMap.get(v.getManagerId()).getAvatarUrl());
            }
            return p;
        }).collect(Collectors.toList());
    }

    public List<WareHouseResp> listEnable(UserInfo user) {
        List<Warehouse> list = wareHouseService.listWareHouseEnable(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = list.stream().map(v -> v.getManagerId()).collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userInfoMap = userInfoList.stream().collect(Collectors.toMap(Account::getId, v -> v));
        return list.stream().map(v -> {
            WareHouseResp p = new WareHouseResp();
            BeanUtils.copyProperties(v, p);

            p.setManagerId(v.getManagerId());
            p.setManagerName(userInfoMap.get(v.getManagerId()).getNickname());
            p.setManagerAvatar(userInfoMap.get(v.getManagerId()).getAvatarUrl());
            return p;
        }).collect(Collectors.toList());
    }
}
