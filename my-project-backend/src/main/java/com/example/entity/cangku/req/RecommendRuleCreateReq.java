package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendRuleCreateReq {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private Long customerId;
    private Long triggerProductId;
    private BigDecimal triggerMinQuantity;
    private BigDecimal triggerMaxQuantity;
    private Integer priority;
    private Integer status;
    private Integer applyScene;
    private Integer ruleType;
    private BigDecimal confidence;
    private String remark;

    private List<RecommendRuleCreateItemReq> ruleItems;

    //租户ID
    private Long tenantId;
    private Long userId;
}