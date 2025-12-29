package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTake;
import com.example.entity.cangku.req.StockListPageReq;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeMapper;
import com.example.service.CkStockTakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:53
 */
@Service
@Slf4j
public class CkStockTakeServiceImpl extends ServiceImpl<CkStockTakeMapper, StockTake> implements CkStockTakeService {

    @Override
    public List<StockTake> selectByWarehouseId(Long warehouseId, Long tenantId) {
        return this.baseMapper.selectList(new QueryWrapper<StockTake>()
                .eq(warehouseId != null, "warehouse_id", warehouseId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public StockTake selectById(Long stockTakeId, Long tenantId) {
        return this.baseMapper.selectOne(new QueryWrapper<StockTake>().eq("id", stockTakeId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Page<StockTake> getStockPage(Page<StockTake> page, StockListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<StockTake>()
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                        .orderByAsc("created_at")
        );
    }
}
