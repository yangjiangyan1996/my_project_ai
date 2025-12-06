package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendRuleCreateItemReq {

    private Long id;

    private Long productId;

    private Integer quantityType;

    private BigDecimal quantityValue;

    private Integer isRequired;

    private BigDecimal confidence;

    private String remark;

    private Integer sequence;
}
