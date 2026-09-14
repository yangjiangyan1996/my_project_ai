package com.example.ai.tool.query.inbound;

import com.example.ai.tool.query.dto.AiQueryDtos.AiInboundOrderResult;
import com.example.ai.tool.query.dto.AiQueryDtos.AiOrderLine;
import com.example.entity.cangku.resp.InboundDetailResp;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.enums.CkInOutboundEnums;

import java.util.ArrayList;
import java.util.List;

final class InboundOrderMapper {

    private InboundOrderMapper() {
    }

    static AiInboundOrderResult fromList(InboundListPageResp r) {
        return AiInboundOrderResult.builder()
                .orderId(r.getId())
                .orderNo(r.getOrderNo())
                .status(r.getStatus())
                .statusName(CkInOutboundEnums.InOutBoundStatus.getDescByCode(r.getStatus()))
                .orderType(r.getOrderType())
                .warehouseId(r.getWarehouseId())
                .warehouseName(r.getWarehouseName())
                .supplierName(r.getSupplierName())
                .totalQuantity(r.getTotalQuantity())
                .itemCount(r.getItemCount())
                .build();
    }

    static AiInboundOrderResult fromDetail(InboundDetailResp d) {
        List<AiOrderLine> lines = new ArrayList<>();
        if (d.getItems() != null) {
            for (InboundDetailResp.InboundDetailCreateReq item : d.getItems()) {
                lines.add(AiOrderLine.builder()
                        .productId(item.getProductId())
                        .sku(item.getSku())
                        .productName(item.getProductName())
                        .quantity(item.getActualQuantity())
                        .batchNo(item.getBatchNo())
                        .build());
            }
        }
        return AiInboundOrderResult.builder()
                .orderId(d.getId())
                .orderNo(d.getOrderNo())
                .status(d.getStatus())
                .statusName(CkInOutboundEnums.InOutBoundStatus.getDescByCode(d.getStatus()))
                .orderType(d.getOrderType())
                .warehouseId(d.getWarehouseId())
                .warehouseName(d.getWarehouseName())
                .supplierName(d.getSupplierName())
                .totalQuantity(d.getTotalQuantity())
                .itemCount(d.getItemCount())
                .items(lines)
                .build();
    }
}
