package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:31
 */
@Data
@TableName("ck_recommend_rule")
public class RecommendRule extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //规则编码（可选，用于人工识别）
    private String ruleCode;
    //规则名称
    private String ruleName;
    //客户ID（NULL表示适用所有客户）
    private Long customerId;
    //触发产品ID
    private Long triggerProductId;
    //最小触发数量
    private BigDecimal triggerMinQuantity;
    //最大触发数量（NULL表示无限制）
    private BigDecimal triggerMaxQuantity;
    //优先级（数字越小优先级越高）
    private Integer priority;
    //是否启用：0-禁用，1-启用
    private Integer isEnabled;
    //应用场景：1-出库推荐，2-采购建议，3-上架建议
    private Integer applyScene;
    //规则类型：1-手动规则，2-自动规则
    private Integer ruleType;
    //规则置信度（0-1，自动规则使用）
    private BigDecimal confidence;
    //备注
    private String remark;
}
