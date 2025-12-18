package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockLock;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkStockLockMapper;
import com.example.service.CkStockLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CkStockLockServiceImpl extends ServiceImpl<CkStockLockMapper, StockLock> implements CkStockLockService {
    
    @Override
    public List<StockLock> findBySourceId(Long tenantId, Long sourceId) {
        LambdaQueryWrapper<StockLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StockLock::getTenantId, tenantId)
                   .eq(StockLock::getSourceId, sourceId)
                   .eq(StockLock::getIsDeleted, 0)
                   .orderByDesc(StockLock::getCreatedAt);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public List<StockLock> findBySourceIdAndLockType(Long tenantId, Long sourceId, Integer lockType) {
        LambdaQueryWrapper<StockLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StockLock::getTenantId, tenantId)
                   .eq(StockLock::getSourceId, sourceId)
                   .eq(StockLock::getLockType, lockType)
                   .eq(StockLock::getIsDeleted, 0)
                   .orderByDesc(StockLock::getCreatedAt);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public BigDecimal sumLockedQuantity(Long tenantId, Long warehouseId, Long productId, 
                                       String batchNo, Long shelfId, Integer lockStatus) {
        
        LambdaQueryWrapper<StockLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StockLock::getTenantId, tenantId)
                   .eq(StockLock::getLockStatus, lockStatus)
                   .eq(StockLock::getIsDeleted, 0);
        
        if (warehouseId != null) {
            queryWrapper.eq(StockLock::getWarehouseId, warehouseId);
        }
        if (productId != null) {
            queryWrapper.eq(StockLock::getProductId, productId);
        }
        if (batchNo != null) {
            queryWrapper.eq(StockLock::getBatchNo, batchNo);
        }
        if (shelfId != null) {
            queryWrapper.eq(StockLock::getShelfId, shelfId);
        }
        
        List<StockLock> locks = this.list(queryWrapper);
        
        return locks.stream()
                .map(lock -> lock.getLockQuantity().subtract(lock.getUnlockQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public boolean batchUpdateStatus(List<Long> lockIds, Integer lockStatus, Long userId) {
        if (lockIds == null || lockIds.isEmpty()) {
            return true;
        }
        
        List<StockLock> locks = this.listByIds(lockIds);
        locks.forEach(lock -> {
            lock.setLockStatus(lockStatus);
            lock.setModifiedBy(userId);
            lock.setModifiedAt(new Date());
        });
        
        return this.updateBatchById(locks);
    }
    
    @Override
    public List<StockLock> findExpiredLocks() {
        LambdaQueryWrapper<StockLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(StockLock::getExpireTime, new Date())
                   .in(StockLock::getLockStatus, 
                       CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode(),
                       CkInOutboundEnums.InventoryLockStatus.PARTIALLY_UNLOCKED.getCode())
                   .eq(StockLock::getIsDeleted, 0);
        
        return this.list(queryWrapper);
    }
}