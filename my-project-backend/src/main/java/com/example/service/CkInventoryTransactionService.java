package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryTransaction;
import com.example.entity.cangku.req.InventoryTransactionListPageReq;

import java.util.List;

// CkInventoryTransactionService.java
public interface CkInventoryTransactionService extends IService<InventoryTransaction> {
    InventoryTransaction getByOrderAndProduct(Long tenantId, Long orderId, Long productId);

    List<InventoryTransaction> selectByProductIds(Long tenantId, List<Long> productIds);

    List<InventoryTransaction> selectByProductId(Long tenantId, Long productId);

    Page<InventoryTransaction> getPage(Page<InventoryTransaction> page, InventoryTransactionListPageReq req);
}