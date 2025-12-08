package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/8 10:57
 */
@Data
public class RecommendRuleQueryReq {
    //客户ID
    private Long customerId;
    //仓库ID
    private Long warehouseId;
    //触发产品ID
    private Long productId;
    //触发产品数量
    private BigDecimal quantity;
    //场景
    private Integer applyScene;
}
