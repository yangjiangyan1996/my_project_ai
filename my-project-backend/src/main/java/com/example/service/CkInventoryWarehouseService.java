package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryWarehouse;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:40
 */
public interface CkInventoryWarehouseService extends IService<InventoryWarehouse> {
    List<InventoryWarehouse> selectByWarehourseId(Long warehouseId, Long tenantId);
    List<InventoryWarehouse> selectByProductId(Long productId, Long tenantId);

    InventoryWarehouse getByWarehouseAndProduct(Long warehouseId, Long productId, Long tenantId);

    List<InventoryWarehouse> getByProductIdsAndWarehouseIds(Long tenantId, List<Long> productIds, List<Long> warehouseIds);

    List<InventoryWarehouse> getByProductIds(Long tenantId, List<Long> productIds);

    List<InventoryWarehouse> selectByTenantId(Long tenantId);
}
