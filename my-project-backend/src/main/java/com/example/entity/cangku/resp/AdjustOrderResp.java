package com.example.entity.cangku.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:44
 */
@Data
public class AdjustOrderResp {
    private Long id;

    private Long tenantId;

    @TableField("adjust_no")
    private String adjustNo;

    @TableField("adjust_type")
    private Integer adjustType;

    @TableField("source_type")
    private Integer sourceType;

    private Long sourceId;

    private String sourceNo;

    private Long warehouseId;

    private String adjustReason;

    @TableField("adjust_status")
    private Integer adjustStatus;

    @TableField("approval_status")
    private Integer approvalStatus;

    private Integer totalItems;

    private BigDecimal totalQuantity;

    private BigDecimal totalCostAmount;

    private BigDecimal totalAmount;

    @TableField("is_affect_cost")
    private Boolean isAffectCost;

    @TableField("is_urgent")
    private Boolean isUrgent;

    private Integer priority;

    private Date expectExecuteTime;

    private Date actualExecuteTime;

    private String remark;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String attachmentUrls;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String extData;

    private Long approverId;

    private Date approveTime;

    private String approveRemark;

    @Version
    private Integer version;
}
