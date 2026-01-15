package com.example.entity.cangku.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

// AdjustDetailResp.java
@Data
@EqualsAndHashCode(callSuper = false)
public class AdjustDetailResp {
    // 主表信息
    private Long id;
    private Long tenantId;
    private String adjustNo;
    private Integer adjustType;
    private Integer sourceType;
    private Long sourceId;
    private String sourceNo;
    private Long warehouseId;
    private String warehouseName;
    private String adjustReason;
    private Integer adjustStatus;
    private Integer totalItems;
    private BigDecimal totalQuantity;
    private BigDecimal totalCostAmount;
    private BigDecimal totalAmount;
    private Boolean isAffectCost;
    private Boolean isUrgent;
    private Integer priority;
    private Date expectExecuteTime;
    private Date actualExecuteTime;
    private String remark;
    private String attachmentUrls;
    private String extData;
    private Long approverId;
    private String approverName;
    private Date approveTime;
    private String approveRemark;
    private Long createdBy;
    private String createdByName;
    private Date createdAt;
    private Long modifiedBy;
    private String modifiedByName;
    private Date modifiedAt;
    
    // 明细列表
    private List<AdjustItemDetail> items;
}

