package com.example.ai.tool.query.stocktake;

import com.example.ai.tool.query.dto.AiQueryDtos.AiStocktakeResult;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.enums.CkStockTakeEnums;

final class StocktakeMapper {

    private StocktakeMapper() {
    }

    static AiStocktakeResult fromList(StockListPageResp r) {
        return AiStocktakeResult.builder()
                .stockTakeId(r.getId())
                .stockTakeNo(r.getStockTakeNo())
                .warehouseId(r.getWarehouseId())
                .warehouseName(r.getWarehouseName())
                .approvalStatus(r.getApprovalStatus())
                .approvalStatusName(CkStockTakeEnums.ApprovalStatus.getDescByCode(r.getApprovalStatus()))
                .takeStatus(r.getTakeStatus())
                .takeStatusName(CkStockTakeEnums.TakeStatus.getDescByCode(r.getTakeStatus()))
                .takeType(r.getTakeType())
                .itemCount(r.getItemCount())
                .remark(r.getRemark())
                .build();
    }

    static AiStocktakeResult fromDetail(StockDetailResp d) {
        return AiStocktakeResult.builder()
                .stockTakeId(d.getId())
                .stockTakeNo(d.getStockTakeNo())
                .warehouseName(d.getWarehouseName())
                .approvalStatusName(d.getApprovalStatusName())
                .takeStatusName(d.getTakeStatusName())
                .takeType(null)
                .remark(d.getRemark())
                .build();
    }
}
