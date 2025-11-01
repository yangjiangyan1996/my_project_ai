package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Inventory;
import com.example.mapper.CkInventoryMapper;
import com.example.service.CkInventoryService;
import org.springframework.stereotype.Service;

// CkInventoryServiceImpl.java
@Service
public class CkInventoryServiceImpl extends ServiceImpl<CkInventoryMapper, Inventory> implements CkInventoryService {


    @Override
    public Inventory getByWarehouseAndProduct(Long warehouseId, Long productId, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<Inventory>()
                .eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }
}