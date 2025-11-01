package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.WarehouseShelf;
import com.example.entity.cangku.req.ShelfListPageReq;
import com.example.mapper.CkShelfMapper;
import com.example.service.CkShelfService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

// CkShelfServiceImpl.java
@Service
public class CkShelfServiceImpl extends ServiceImpl<CkShelfMapper, WarehouseShelf> implements CkShelfService {

    @Override
    public List<WarehouseShelf> selectByCodeOrName(String shelfCode, String shelfName, Long tenantId) {
        // 构造查询条件
        LambdaQueryWrapper<WarehouseShelf> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WarehouseShelf::getTenantId, tenantId)
                .and(wrapper -> wrapper
                        .eq(StringUtils.isNotBlank(shelfCode), WarehouseShelf::getShelfCode, shelfCode)
                        .or()
                        .eq(StringUtils.isNotBlank(shelfName), WarehouseShelf::getShelfName, shelfName));

        // 查询结果
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<WarehouseShelf> listWareHouseEnable(Long tenantId, Long warehouseId) {
        return baseMapper.selectList(new QueryWrapper<WarehouseShelf>()
                .eq("is_deleted", 0)
                .eq("status", 1)
                .eq("warehouse_id", warehouseId));
    }

    @Override
    public Page<WarehouseShelf> getPage(Page<WarehouseShelf> page, ShelfListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<WarehouseShelf>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .eq(req.getWarehouseId()!= null ,"warehouse_id", req.getWarehouseId())
                        .like(StringUtils.isNotBlank(req.getArea() ), "area", req.getArea())
                        .like(StringUtils.isNotBlank(req.getShelfCode() ), "shelf_code", req.getShelfCode())
                        .like(StringUtils.isNotBlank(req.getShelfName()) , "shelf_name", req.getShelfName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("sort_order")
        );
    }

}