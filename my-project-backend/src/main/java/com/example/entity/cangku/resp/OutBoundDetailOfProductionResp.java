package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/24 20:16
 */
@Data
public class OutBoundDetailOfProductionResp {
    // 基本信息
    private Long id;
    private String orderNo;
    private Integer orderType;
    private Long warehouseId;
    private String warehouseName;
    private String expectedDate;
    private String relatedOrderNo;
    private String remark;
    private Integer status;

    // 统计信息
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal totalAmountUsd;

    // 系统信息
    private Date createdAt;
    private Date updatedAt;
    private String applicantName;
    private Long userId;
    private Long tenantId;

    // 产品明细 - 简化结构，专注于生产领料需求
    private List<ProductionProductItem> items;

    @Data
    public static class ProductionProductItem {
        // 产品基本信息
        private Long productId;
        private String productName;
        private String sku;
        private String spec;
        private String unit;
        private String color;
        private BigDecimal quantity;

        // BOM相关数据
        private List<BomComponent> bomComponents;
        private List<MaterialAllocation> materialAllocations;
    }

    @Data
    public static class BomComponent {
        // 原料组件信息
        private Long id;
        private Long componentProductId;
        private String componentProductName;
        private String componentProductSku;
        private String componentProductSpec;
        private String componentProductUnit;
        private BigDecimal unitUsage; // 单件用量
        private Integer typeForSort;

        // 库存批次信息
        private List<StockBatch> availableBatches;

        //相同原料合并的详情
        List<UsageDetail> usageDetailList;
    }

    @Data
    public static class StockBatch {
        private String batchNo;
        private BigDecimal totalQuantity;
        private List<StockShelf> shelves;
    }

    @Data
    public static class StockShelf {
        private Long shelfId;
        private String shelfName;
        private Long shelivesId;
        private String shelivesName;
        private BigDecimal availableQuantity;
    }

    @Data
    public static class MaterialAllocation {
        // 分配信息
        private Long componentProductId;
        private String batchNo;
        private Long shelfId;
        private String shelfName;
        private Long shelivesId;
        private String shelivesName;

        private BigDecimal allocatedQuantity;
    }

    /**
     * bomdetail的合并相同原料的用量信息
     */
    @Data
    public static class UsageDetail {
        private Long bomDetailId;
        private BigDecimal quantity;
        private Integer type;
        private BigDecimal lossRate;
        private String remark;
        private Integer sortOrder;
    }
}
