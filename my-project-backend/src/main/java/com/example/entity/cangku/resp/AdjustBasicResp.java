package com.example.entity.cangku.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

// AdjustBasicResp.java
@Data
@EqualsAndHashCode(callSuper = false)
public class AdjustBasicResp {
    private Long id;
    private String adjustNo;
    private Long warehouseId;
    private String warehouseName;
    private Integer adjustType;
    private Integer sourceType;
    private String sourceNo;
    private String adjustReason;
    private Integer adjustStatus;
    private Integer totalItems;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private Date expectExecuteTime;
    private Date actualExecuteTime;
    private String remark;
    private Long approverId;
    private String approverName;
    private Date approveTime;
    private Long createdBy;
    private String createdByName;
    private Date createdAt;
}