package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.StockTakeSnapshot;
import com.example.enums.CkCommonEnums;
import com.example.mapper.CkStockTakeSnapshotMapper;
import com.example.service.CkStockTakeSnapshotService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/26 23:11
 */
@Service
public class CkStockTakeSnapshotServiceImpl  extends ServiceImpl<CkStockTakeSnapshotMapper, StockTakeSnapshot> implements CkStockTakeSnapshotService {
    @Override
    public List<StockTakeSnapshot> selectSnapshotByStockId(Long stockTakeId, Long tenantId) {
        return baseMapper.selectList(new QueryWrapper<StockTakeSnapshot>()
                .eq("stock_take_id",stockTakeId)
                .eq("tenant_id",tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }
}
