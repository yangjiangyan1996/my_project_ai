package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryTransaction;

// CkInventoryTransactionService.java
public interface CkInventoryTransactionService extends IService<InventoryTransaction> {
    InventoryTransaction getByOrderAndProduct(Long tenantId, Long orderId, Long productId);
}