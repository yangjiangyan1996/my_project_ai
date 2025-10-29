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
    public Page<WarehouseShelf> getPage(Page<WarehouseShelf> page, ShelfListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<WarehouseShelf>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .like(StringUtils.isNotBlank(req.getCode() ), "shelf_code", req.getCode())
                        .like(StringUtils.isNotBlank(req.getName()) , "shelf_name", req.getName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("id")
        );
    }

}