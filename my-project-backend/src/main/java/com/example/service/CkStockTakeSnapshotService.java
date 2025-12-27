package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.StockTakeSnapshot;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/26 23:11
 */
public interface CkStockTakeSnapshotService  extends IService<StockTakeSnapshot> {
    List<StockTakeSnapshot> selectSnapshotByStockId(Long stockTakeId, Long tenantId);
}
