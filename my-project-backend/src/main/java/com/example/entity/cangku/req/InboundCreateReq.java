
package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class InboundCreateReq {
    private Long id;
    private String orderNo;
    private Integer orderType;
    // 关联单号 - 调整：从必填改为可选，因为现在关联信息在明细中
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private Long supplierId;
    private Double totalQuantity;
    private Long warehouseId;
    // 总价
    private BigDecimal totalAmount;

    // 新增字段：预计入库日期
    private String expectedDate;

    // 产品明细
    private List<InboundDetailCreateReq> items;

    private Long tenantId;
    private Long userId;

    @Data
    public static class InboundDetailCreateReq {
        private Double actualQuantity;
        // 批次号
        private String batchNo;
        private Long productId;
        private String remark;

        // 调整：shelfLocationId 改为可选，因为现在使用 shelfLocationIds 数组
        private String shelfLocationId;

        // 单价
        private BigDecimal priceUnit;
        // 总价
        private BigDecimal priceTotal;

        // 货架分配
        private List<ShelfDetailCreateReq> shelfAllocations;

        // 新增字段：产品名称
        private String productName;
        // 新增字段：SKU
        private String sku;
        // 新增字段：规格型号
        private String spec;
        // 新增字段：单位
        private String unit;

        // 新增字段：关联领料单号（现在关联信息在明细级别）
        private String relatedPickingOrderNo;

        // 新增字段：货架位置ID数组（多选）
        private List<Long> shelfLocationIds;

        // 新增字段：生产任务ID（用于关联生产任务）
        //private Long productionTaskId;
    }

    @Data
    public static class ShelfDetailCreateReq {
        private Long shelfLocationId;
        private Double quantity;
    }
}
