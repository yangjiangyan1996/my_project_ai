package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InventoryLock;

import java.math.BigDecimal;
import java.util.List;

public interface CkInventoryLockService extends IService<InventoryLock> {
    
    /**
     * 根据来源单据ID查询锁定记录
     */
    List<InventoryLock> findBySourceId(Long tenantId, Long sourceId);
    
    /**
     * 根据来源单据ID和锁定类型查询
     */
    List<InventoryLock> findBySourceIdAndLockType(Long tenantId, Long sourceId, Integer lockType);
    
    /**
     * 查询指定条件的锁定数量总和
     */
    BigDecimal sumLockedQuantity(Long tenantId, Long warehouseId, Long productId, 
                                 String batchNo, Long shelfId, Integer lockStatus);
    
    /**
     * 批量更新锁定状态
     */
    boolean batchUpdateStatus(List<Long> lockIds, Integer lockStatus, Long userId);
    
    /**
     * 查询过期的锁定记录
     */
    List<InventoryLock> findExpiredLocks();
}