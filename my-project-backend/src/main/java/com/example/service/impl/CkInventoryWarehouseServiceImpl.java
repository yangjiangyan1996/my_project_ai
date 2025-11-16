package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryWarehouse;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkInventoryWarehouseMapper;
import com.example.service.CkInventoryWarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:41
 */
@Service
public class CkInventoryWarehouseServiceImpl  extends ServiceImpl<CkInventoryWarehouseMapper, InventoryWarehouse> implements CkInventoryWarehouseService {
    @Override
    public List<InventoryWarehouse> selectByProductId(Long productId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryWarehouse>()
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<InventoryWarehouse> selectByWarehourseId(Long warehouseId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryWarehouse>()
                .eq("warehouse_id", warehouseId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<InventoryWarehouse> getByProductIdsAndWarehouseIds(Long tenantId, List<Long> productIds, List<Long> warehouseIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryWarehouse>()
                .eq("tenant_id", tenantId)
                .in("product_id", productIds)
                .in("warehouse_id", warehouseIds)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<InventoryWarehouse> selectByTenantId(Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<InventoryWarehouse>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public List<InventoryWarehouse> getByProductIds(Long tenantId, List<Long> productIds) {
        return baseMapper.selectList(new QueryWrapper<InventoryWarehouse>()
                .eq("tenant_id", tenantId)
                .in("product_id", productIds)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public InventoryWarehouse getByWarehouseAndProduct(Long warehouseId, Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<InventoryWarehouse>()
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }
}
