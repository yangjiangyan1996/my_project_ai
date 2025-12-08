package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:20
 */
@Data
public class ReCommendRulePageListResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //规则编码（可选，用于人工识别）
    private String ruleCode;
    //规则名称
    private String ruleName;
    //客户ID（NULL表示适用所有客户）
    private Long customerId;
    //客户名称
    private String customerName;
    //触发产品名称
    private String triggerProductName;
    //触发产品SKU
    private String triggerProductSku;
    //触发产品规格
    private String triggerProductSpec;
    //触发产品颜色
    private String triggerProductColor;
    //触发产品ID
    private Long triggerProductId;
    //最小触发数量
    private BigDecimal triggerMinQuantity;
    //最大触发数量（NULL表示无限制）
    private BigDecimal triggerMaxQuantity;
    //优先级（数字越小优先级越高）
    private Integer priority;
    //是否启用：0-禁用，1-启用
    private Integer status;
    //应用场景：1-出库推荐，2-采购建议，3-上架建议
    private Integer applyScene;
    //规则类型：1-手动规则，2-自动规则
    private Integer ruleType;
    //规则置信度（0-1，自动规则使用）
    private BigDecimal confidence;
    //备注
    private String remark;

    //规则项数量
    private Long itemCount;

    private Date createdAt;

    private Date modifiedAt;

}
