package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryBatch;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkInventoryBatchMapper;
import com.example.service.CkInventoryBatchService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 00:14
 */
@Service
public class CkInventoryBatchServiceImpl extends ServiceImpl<CkInventoryBatchMapper, InventoryBatch> implements CkInventoryBatchService {
    @Override
    public List<InventoryBatch> selectByWarehouseId(Long warehouseId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryBatch>()
                .eq("warehouse_id", warehouseId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
        );
    }

    @Override
    public InventoryBatch selectByProductBatchWarehouse(Long tenantId, Long productId, String batchNo, Long warehouseId) {
        return baseMapper.selectOne(new QueryWrapper<InventoryBatch>()
                .eq("product_id", productId)
                .eq("batch_no", batchNo)
                .eq("warehouse_id", warehouseId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
        );
    }

    @Override
    public List<InventoryBatch> selectByProductIdsAndWarehouseIds(Long tenantId, List<Long> productIds, List<Long> warehouseIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryBatch>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                .in("product_id", productIds)
                .in("warehouse_id", warehouseIds)
        );
    }

    @Override
    public InventoryBatch selectByProductId(Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<InventoryBatch>()
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                );
    }

    @Override
    public List<InventoryBatch> selectByWarehouseIdAndProductId(Long warehouseId, Long productId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryBatch>()
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                .orderByDesc("production_date"));
    }

    @Override
    public InventoryBatch selectByBatchNoAndProductId(String batchNo, Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<InventoryBatch>()
                .eq("batch_no", batchNo)
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
        );
    }
}
