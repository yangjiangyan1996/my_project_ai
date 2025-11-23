package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryShelf;

import java.util.List;
import java.util.Set;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/15 22:42
 */
public interface CkInventoryShelfService extends IService<InventoryShelf> {
    List<InventoryShelf> selectByTenantId(Long tenantId);

    InventoryShelf getByWarehouseAndProductAndShelf(Long warehouseId, Long productId, Long shelfLocationId,String batchNo, Long tenantId);

    List<InventoryShelf> selectByProductIds(Long productId, Long tenantId);

    List<InventoryShelf> getBatchShelfStock(Long tenantId,Long warehouseId, Set<Long> componentProductIds);
}
