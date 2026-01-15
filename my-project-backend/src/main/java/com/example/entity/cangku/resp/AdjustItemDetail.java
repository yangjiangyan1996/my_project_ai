package com.example.entity.cangku.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

// AdjustItemDetail.java
@Data
@EqualsAndHashCode(callSuper = false)
public class AdjustItemDetail {
    private Long id;
    private Long tenantId;
    private Long adjustOrderId;
    private String adjustNo;
    private Long productId;
    private String productCode;
    private String productName;
    private String skuCode;
    private String specification;
    private String unit;
    private Long warehouseId;
    private String warehouseName;
    private String batchNo;
    private Long shelfId;
    private String shelfCode;
    private String locationCode;
    private Integer inventoryType;
    private BigDecimal beforeQuantity;
    private BigDecimal adjustQuantity;
    private BigDecimal afterQuantity;
    private BigDecimal unitCost;
    private BigDecimal adjustCostAmount;
    private BigDecimal unitPrice;
    private BigDecimal adjustAmount;
    private String adjustReason;
    private String itemRemark;
    private Integer status;
    private Date executeTime;
    private Long executeBy;
    private String executeByName;
    private Boolean isAffectCost;
    private Integer costAdjustMethod;
    private Long sourceItemId;
    private String extData;
    private Long createdBy;
    private String createdByName;
    private Date createdAt;
}