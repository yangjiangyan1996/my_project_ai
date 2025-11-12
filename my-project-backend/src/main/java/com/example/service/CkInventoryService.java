package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Inventory;
import com.example.entity.cangku.req.InventoryListPageReq;

import java.util.List;

// CkInventoryService.java
public interface CkInventoryService extends IService<Inventory> {
    Inventory getByProduct(Long productId, Long tenantId);

    Page<Inventory> getPage(Page<Inventory> page, InventoryListPageReq req);

    List<Inventory> selectByProductIds(Long tenantId, List<Long> productIds);

    Long selectPageListCount(InventoryListPageReq req);

}