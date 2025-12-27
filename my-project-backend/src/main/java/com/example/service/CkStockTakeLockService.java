package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.StockTakeLock;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:54
 */
public interface CkStockTakeLockService extends IService<StockTakeLock> {
    Boolean getStockLockExist(Long stockTakeId, Long tenantId, int lockStatus);
}
