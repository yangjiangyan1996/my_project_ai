package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryBatch;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 00:14
 */
public interface CkInventoryBatchService extends IService<InventoryBatch> {
    InventoryBatch selectByBatchNoAndProductId(String batchNo, Long productId, Long tenantId);

    List<InventoryBatch> selectByWarehouseIdAndProductId(Long warehouseId, Long productId, Long tenantId);
}
