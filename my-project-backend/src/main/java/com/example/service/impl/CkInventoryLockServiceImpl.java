package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InventoryLock;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkInventoryLockMapper;
import com.example.service.CkInventoryLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CkInventoryLockServiceImpl extends ServiceImpl<CkInventoryLockMapper, InventoryLock> implements CkInventoryLockService {
    
    @Override
    public List<InventoryLock> findBySourceId(Long tenantId, Long sourceId) {
        LambdaQueryWrapper<InventoryLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryLock::getTenantId, tenantId)
                   .eq(InventoryLock::getSourceId, sourceId)
                   .eq(InventoryLock::getIsDeleted, 0)
                   .orderByDesc(InventoryLock::getCreatedAt);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public List<InventoryLock> findBySourceIdAndLockType(Long tenantId, Long sourceId, Integer lockType) {
        LambdaQueryWrapper<InventoryLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryLock::getTenantId, tenantId)
                   .eq(InventoryLock::getSourceId, sourceId)
                   .eq(InventoryLock::getLockType, lockType)
                   .eq(InventoryLock::getIsDeleted, 0)
                   .orderByDesc(InventoryLock::getCreatedAt);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public BigDecimal sumLockedQuantity(Long tenantId, Long warehouseId, Long productId, 
                                       String batchNo, Long shelfId, Integer lockStatus) {
        
        LambdaQueryWrapper<InventoryLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryLock::getTenantId, tenantId)
                   .eq(InventoryLock::getLockStatus, lockStatus)
                   .eq(InventoryLock::getIsDeleted, 0);
        
        if (warehouseId != null) {
            queryWrapper.eq(InventoryLock::getWarehouseId, warehouseId);
        }
        if (productId != null) {
            queryWrapper.eq(InventoryLock::getProductId, productId);
        }
        if (batchNo != null) {
            queryWrapper.eq(InventoryLock::getBatchNo, batchNo);
        }
        if (shelfId != null) {
            queryWrapper.eq(InventoryLock::getShelfId, shelfId);
        }
        
        List<InventoryLock> locks = this.list(queryWrapper);
        
        return locks.stream()
                .map(lock -> lock.getLockQuantity().subtract(lock.getUnlockQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public boolean batchUpdateStatus(List<Long> lockIds, Integer lockStatus, Long userId) {
        if (lockIds == null || lockIds.isEmpty()) {
            return true;
        }
        
        List<InventoryLock> locks = this.listByIds(lockIds);
        locks.forEach(lock -> {
            lock.setLockStatus(lockStatus);
            lock.setModifiedBy(userId);
            lock.setModifiedAt(new Date());
        });
        
        return this.updateBatchById(locks);
    }
    
    @Override
    public List<InventoryLock> findExpiredLocks() {
        LambdaQueryWrapper<InventoryLock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(InventoryLock::getExpireTime, new Date())
                   .in(InventoryLock::getLockStatus,
                       CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode(),
                       CkInOutboundEnums.InventoryLockStatus.PARTIALLY_UNLOCKED.getCode())
                   .eq(InventoryLock::getIsDeleted, 0);
        
        return this.list(queryWrapper);
    }
}