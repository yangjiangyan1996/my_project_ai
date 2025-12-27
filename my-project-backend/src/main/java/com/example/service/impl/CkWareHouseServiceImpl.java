package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Warehouse;
import com.example.entity.cangku.req.WareHouseListPageReq;
import com.example.mapper.CkWareHouseMapper;
import com.example.service.CkWareHouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:27
 */
@Service
public class CkWareHouseServiceImpl extends ServiceImpl<CkWareHouseMapper, Warehouse> implements CkWareHouseService {
    @Override
    public Page<Warehouse> getPage(Page<Warehouse> page, WareHouseListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Warehouse>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .like(StringUtils.isNotBlank(req.getCode() ), "code", req.getCode())
                        .like(StringUtils.isNotBlank(req.getName()) , "name", req.getName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("id")
        );
    }

    @Override
    public List<Warehouse> listWareHouse(Long tenantId) {
        return baseMapper.selectList(
                new QueryWrapper<Warehouse>()
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted",0)
                        .orderByAsc("id")
        );
    }

    @Override
    public List<Warehouse> selectByCodeOrName(String code, String name, Long tenantId) {
        // 构造查询条件
        LambdaQueryWrapper<Warehouse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Warehouse::getTenantId, tenantId)
                .and(wrapper -> wrapper
                        .eq(StringUtils.isNotBlank(code), Warehouse::getCode, code)
                        .or()
                        .eq(StringUtils.isNotBlank(name), Warehouse::getName, name));

        // 查询结果
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Long selectCountsOfWareHouses(Long tenantId) {
        return baseMapper.selectCount(new QueryWrapper<Warehouse>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<Warehouse> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Warehouse>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }

    @Override
    public Warehouse selectByTenantIdAndWareHouseId(Long tenantId, Long wareHouseId) {
        return baseMapper.selectOne(new QueryWrapper<Warehouse>()
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)
                .eq("id", wareHouseId));
    }

    @Override
    public List<Warehouse> selectByTenantIdAndWareHouseIds(Long tenantId, List<Long> wareHouseIds) {
        if (wareHouseIds == null || wareHouseIds.isEmpty()) {
            return new ArrayList<>();
        }
        return baseMapper.selectList(new QueryWrapper<Warehouse>()
                .eq("is_deleted", 0).eq("tenant_id", tenantId).in("id", wareHouseIds));
    }

    @Override
    public List<Warehouse> listWareHouseEnable(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Warehouse>()
                .eq("is_deleted", 0)
                .eq("status", 1)
                .eq("tenant_id", tenantId));
    }

    @Override
    public List<Warehouse> getByIds(List<Long> whIds, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<Warehouse>()
                .in("id", whIds)
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId));
    }
}
