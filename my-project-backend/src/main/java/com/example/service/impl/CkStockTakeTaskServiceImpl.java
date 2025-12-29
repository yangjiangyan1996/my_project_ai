package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTakeTask;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeTaskMapper;
import com.example.service.CkStockTakeTaskService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 12:53
 */
@Service
public class CkStockTakeTaskServiceImpl   extends ServiceImpl<CkStockTakeTaskMapper, StockTakeTask> implements CkStockTakeTaskService {

    @Override
    public List<StockTakeTask> selectByStockTakeIdAndExecutorId(Long tenantId, Long stockTakeId, Long userId) {
        return baseMapper.selectList(new QueryWrapper<StockTakeTask>().eq("stock_take_id", stockTakeId)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                .eq("executor_id", userId));
    }

    @Override
    public Boolean deletedByStockId(Long stockTakeId, Long tenantId, Long userId) {
        StockTakeTask stockTake = new StockTakeTask();
        stockTake.setIsDeleted(CkCommonEnums.IsDeleted.Delete.getCode());
        stockTake.setModifiedBy(userId);
        stockTake.setModifiedAt(new Date());
        return baseMapper.update(stockTake,
                new QueryWrapper<StockTakeTask>().eq("stock_take_id", stockTakeId)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())) > 0;
    }
}
