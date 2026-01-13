package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Shelives;
import com.example.entity.cangku.req.ShelfListPageReq;
import com.example.mapper.CkShelivesMapper;
import com.example.service.CkShelivesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2026/1/2 22:18
 */
@Service
public class CkShelivesServiceImpl extends ServiceImpl<CkShelivesMapper, Shelives> implements CkShelivesService {
    @Override
    public Shelives selectById(Long id, Long tenantId) {
        return  baseMapper.selectOne(
                new QueryWrapper<Shelives>()
                        .eq("id", id)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted",0)
        );
    }

    @Override
    public List<Shelives> selectByIds(Long tenantId, List<Long> ids) {
        return baseMapper.selectList(
                new QueryWrapper<Shelives>()
                        .eq("tenant_id", tenantId)
                        .in("id", ids)
                        .eq("is_deleted",0)
        );
    }

    @Override
    public Page<Shelives> getPage(Page<Shelives> page, ShelfListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<Shelives>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .eq(req.getWarehouseId()!= null ,"warehouse_id", req.getWarehouseId())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("sort_order")
        );
    }
}
