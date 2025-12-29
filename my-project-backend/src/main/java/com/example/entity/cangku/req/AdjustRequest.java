package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AdjustRequest {
    private Long id;
    private String adjustNo;
    private Long warehouseId;
    private String warehouseName;
    private Integer adjustType; // 1-盘点调整、2-报损调整、3-报溢调整、4-成本调整、5-库存转移、6-其他调整
    private Integer sourceType; // 1-盘点单、2-手动创建、3-异常处理、4-系统自动
    private Long sourceId;
    private String sourceNo;
    private String adjustReason;
    private Boolean isUrgent;
    private Boolean isAffectCost;
    private Integer priority; // 1-紧急、2-高、3-中、4-低
    private Date expectExecuteTime;
    private String remark;
    private Long userId;
    private Long tenantId;
    private List<AdjustItemRequest> items;

    @Data
    public static class AdjustItemRequest {
        private Long productId;
        private String productName;
        private String productCode;
        private String skuCode;
        private String specification;
        private String unit;
        private String batchNo;
        private Long shelfId;
        private String shelfCode;
        private String locationCode;
        private BigDecimal beforeQuantity;
        private BigDecimal adjustQuantity; // 正数表示增加，负数表示减少
        private BigDecimal afterQuantity;
        private BigDecimal unitCost;
        private BigDecimal adjustCostAmount;
        private BigDecimal unitPrice;
        private BigDecimal adjustAmount;
        private String itemReason;
        private String itemRemark;
        private Integer inventoryType; // 1-普通库存、2-批次库存、3-货架库存
        private Integer costAdjustMethod; // 1-加权平均、2-移动平均、3-指定批次
        private Long sourceItemId;
        private String extData;
    }
}