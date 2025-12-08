package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/8 10:58
 */
@Data
public class RecommendRuleQueryResp {
    private Long productId;

    private List<RecommendRuleInner> recommendations;
    @Data
    public static class RecommendRuleInner {
        //推荐产品ID,
        private Long productId;
        //产品名称
        private String productName;
        //SKU
        private String sku;
        //规格
        private String spec;
        //颜色
        private String color;
        // 1-固定数量，2-比例
        private Integer quantityType;
        //数量或比例
        private BigDecimal quantityValue;
        //是否必选
        private Boolean isRequired;
        //置信度
        private BigDecimal confidence;
        //推荐说明
        private String remark;
    }
}
