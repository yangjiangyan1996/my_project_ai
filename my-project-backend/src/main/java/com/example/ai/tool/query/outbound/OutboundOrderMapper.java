package com.example.ai.tool.query.outbound;

import com.example.ai.tool.query.dto.AiQueryDtos.AiOrderLine;
import com.example.ai.tool.query.dto.AiQueryDtos.AiOutboundOrderResult;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.enums.CkInOutboundEnums;

import java.util.ArrayList;
import java.util.List;

final class OutboundOrderMapper {

    private OutboundOrderMapper() {
    }

    static AiOutboundOrderResult fromList(OutboundListPageResp r) {
        return AiOutboundOrderResult.builder()
                .orderId(r.getId())
                .orderNo(r.getOrderNo())
                .status(r.getStatus())
                .statusName(CkInOutboundEnums.InOutBoundStatus.getDescByCode(r.getStatus()))
                .orderType(r.getOrderType())
                .warehouseId(r.getWarehouseId())
                .warehouseName(r.getWarehouseName())
                .customerName(r.getCustomerName())
                .totalQuantity(r.getTotalQuantity())
                .itemCount(r.getItemCount())
                .build();
    }

    static AiOutboundOrderResult fromDetail(OutboundDetailResp d) {
        List<AiOrderLine> lines = new ArrayList<>();
        if (d.getItems() != null) {
            for (OutboundDetailResp.ProductInfoInner item : d.getItems()) {
                lines.add(AiOrderLine.builder()
                        .productId(item.getProductId())
                        .sku(item.getSku())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .batchNo(item.getBatchNo())
                        .build());
            }
        }
        return AiOutboundOrderResult.builder()
                .orderId(d.getId())
                .orderNo(d.getOrderNo())
                .status(d.getStatus())
                .statusName(CkInOutboundEnums.InOutBoundStatus.getDescByCode(d.getStatus()))
                .orderType(d.getOrderType())
                .warehouseId(d.getWarehouseId())
                .warehouseName(d.getWarehouseName())
                .customerName(d.getCustomerName())
                .totalQuantity(d.getTotalQuantity())
                .itemCount(d.getItems() == null ? 0 : d.getItems().size())
                .items(lines)
                .build();
    }
}
