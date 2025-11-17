package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/17 23:37
 */
@Data
public class OutBoundSaleQuantityImportResp {
    private Long productId;

    private String sku;

    private String productName;

    private BigDecimal quantity;

    private BigDecimal price;

    private String remark;
}
