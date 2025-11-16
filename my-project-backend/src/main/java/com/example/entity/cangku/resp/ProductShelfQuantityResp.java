package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/15 23:10
 */
@Data
public class ProductShelfQuantityResp {
    //货架 ID
    private Long shelfId;
    //货架名称
    private String shelfName;
    //货架库存
    private BigDecimal shelfQuantity;
    //货架可用库存
    private BigDecimal shelfAvailableQuantity;
}
