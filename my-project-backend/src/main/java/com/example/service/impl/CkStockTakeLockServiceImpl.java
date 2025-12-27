package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTakeLock;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeLockMapper;
import com.example.service.CkStockTakeLockService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:54
 */
@Service
public class CkStockTakeLockServiceImpl extends ServiceImpl<CkStockTakeLockMapper, StockTakeLock> implements CkStockTakeLockService {
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
