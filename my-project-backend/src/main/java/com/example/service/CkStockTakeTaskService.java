package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.StockTakeTask;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 12:53
 */
public interface CkStockTakeTaskService extends IService<StockTakeTask> {
    Boolean deletedByStockId(Long stockTakeId, Long tenantId, Long userId);

    List<StockTakeTask> selectByStockTakeIdAndExecutorId(Long tenantId, Long stockTakeId, Long userId);
}
