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
@TableName("ck_recommend_rule_item")
public class RecommendRuleItem extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //规则ID
    private Long ruleId;
    //推荐产品ID
    private Long productId;
    //数量类型：1-固定数量，2-按比例（相对于触发产品）
    private Integer quantityType;
    //数量或者比例（quantity_type=1时是固定数量的内容，quantity_type=2时是比例数量（比如1个触发产品对应10个推荐产品，则这里是10））
    private BigDecimal quantityValue;
    //是否强制推荐：0-建议，1-强制
    private Integer isRequired;
    //置信度（0-1，自动规则使用）
    private BigDecimal confidence;
    //显示顺序
    private Integer sequence;

    private String remark;
}
