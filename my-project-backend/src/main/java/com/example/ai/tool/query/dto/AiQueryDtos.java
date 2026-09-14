package com.example.ai.tool.query.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Token-friendly AI query DTOs. No tenantId / internal dump.
 */
public final class AiQueryDtos {

    private AiQueryDtos() {
    }

    @Data
    @Builder
    public static class AiProductResult {
        private Long productId;
        private String sku;
        private String name;
        private String spec;
        private String color;
        private String unitName;
        private Integer status;
    }

    @Data
    @Builder
    public static class AiWarehouseResult {
        private Long warehouseId;
        private String code;
        private String name;
        private String address;
        private Integer status;
        private Integer type;
    }

    @Data
    @Builder
    public static class AiWarehouseStock {
        private Long warehouseId;
        private String warehouseName;
        private BigDecimal quantity;
        private BigDecimal lockedQuantity;
        /** Copied from Facade when present; Tool does not compute. */
        private BigDecimal availableQuantity;
    }

    @Data
    @Builder
    public static class AiShelfStock {
        private Long shelfId;
        private String shelfName;
        private BigDecimal quantity;
    }

    @Data
    @Builder
    public static class AiBatchStock {
        private String batchNo;
        private BigDecimal quantity;
        private List<AiShelfStock> shelves;
    }

    @Data
    @Builder
    public static class AiInventoryResult {
        private Long productId;
        private String sku;
        private String productName;
        private String spec;
        private String fieldSource;
        private List<AiWarehouseStock> warehouses;
        private List<AiShelfStock> shelves;
        private BigDecimal remainingStockQuantityOfAllWarehouses;
    }

    @Data
    @Builder
    public static class AiOrderLine {
        private Long productId;
        private String sku;
        private String productName;
        private BigDecimal quantity;
        private String batchNo;
    }

    @Data
    @Builder
    public static class AiInboundOrderResult {
        private Long orderId;
        private String orderNo;
        private Integer status;
        private String statusName;
        private Integer orderType;
        private Long warehouseId;
        private String warehouseName;
        private String supplierName;
        private BigDecimal totalQuantity;
        private Integer itemCount;
        private List<AiOrderLine> items;
    }

    @Data
    @Builder
    public static class AiOutboundOrderResult {
        private Long orderId;
        private String orderNo;
        private Integer status;
        private String statusName;
        private Integer orderType;
        private Long warehouseId;
        private String warehouseName;
        private String customerName;
        private BigDecimal totalQuantity;
        private Integer itemCount;
        private List<AiOrderLine> items;
    }

    @Data
    @Builder
    public static class AiStocktakeResult {
        private Long stockTakeId;
        private String stockTakeNo;
        private Long warehouseId;
        private String warehouseName;
        private Integer approvalStatus;
        private String approvalStatusName;
        private Integer takeStatus;
        private String takeStatusName;
        private Integer takeType;
        private Integer itemCount;
        private String remark;
    }
}
