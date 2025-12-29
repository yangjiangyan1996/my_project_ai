package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTakeLock;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeLockMapper;
import com.example.service.CkStockTakeLockService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:54
 */
@Service
public class CkStockTakeLockServiceImpl extends ServiceImpl<CkStockTakeLockMapper, StockTakeLock> implements CkStockTakeLockService {
    @Override
    public Boolean releaseByStockTakeId(Long stockTakeId,Long userId,  Long tenantId) {
        StockTakeLock stockTakeLock = new StockTakeLock();
        stockTakeLock.setLockStatus(CkCommonEnums.LockStatus.Released.getCode());
        stockTakeLock.setModifiedBy(userId);
        stockTakeLock.setModifiedAt(new Date());
        stockTakeLock.setUnlockTime(new Date());

        return this.baseMapper.update( stockTakeLock, new LambdaQueryWrapper<StockTakeLock>()
                .eq(StockTakeLock::getStockTakeId, stockTakeId)
                .eq(StockTakeLock::getTenantId, tenantId)
                .eq(StockTakeLock::getIsDeleted, CkCommonEnums.IsDeleted.NoDelete.getCode())) > 0;
    }

    @Override
    public Boolean getLockExist(Integer takeScope, Long warehouseId, Long tenantId, int lockStatus) {
        return this.baseMapper.exists(new LambdaQueryWrapper<StockTakeLock>()
                .eq(StockTakeLock::getLockScope, takeScope)
                .eq(StockTakeLock::getLockStatus, lockStatus)
                .eq(StockTakeLock::getWarehouseId, warehouseId)
                .eq(StockTakeLock::getTenantId, tenantId)
                .eq(StockTakeLock::getIsDeleted, CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Boolean getStockLockExist(Long stockTakeId, Long tenantId, int lockStatus) {
        StockTakeLock stockTakeLock = this.baseMapper.selectOne(new LambdaQueryWrapper<StockTakeLock>()
                .eq(StockTakeLock::getStockTakeId, stockTakeId)
                .eq(StockTakeLock::getLockStatus, lockStatus)
                .eq(StockTakeLock::getIsDeleted, CkCommonEnums.IsDeleted.NoDelete.getCode())
                .eq(StockTakeLock::getTenantId, tenantId));
        return stockTakeLock != null;
    }
}
