package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Inventory;

// CkInventoryService.java
public interface CkInventoryService extends IService<Inventory> {
    Inventory getByWarehouseAndProduct(Long warehouseId, Long productId, Long tenantId);
}