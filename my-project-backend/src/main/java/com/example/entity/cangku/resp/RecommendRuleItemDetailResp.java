package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendRuleItemDetailResp {

    private Long id;

    private Long productId;
    private String productName;
    private String productSku;
    private String productColor;
    private String productSpec;

    private Integer quantityType;

    private BigDecimal quantityValue;

    private Integer isRequired;

    private BigDecimal confidence;

    private String remark;

    private Integer sequence;
}
