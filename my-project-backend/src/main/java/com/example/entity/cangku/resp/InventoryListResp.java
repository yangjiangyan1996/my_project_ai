package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

//供应商表
@Data
public class InventoryListResp {

    //产品ID
    private Long productId;

    //产品名称
    private String productName;

    //规格型号
    private String spec;

    //颜色
    private String color;

    //单位ID
    private String unitName;

    //可用库存
    private BigDecimal availableQuantity;
    //总库存
    private BigDecimal quantity;
    //锁定库存
    private BigDecimal lockedQuantity;

    //出货单位（比如按箱子出货
    private String outUnitName;

    //出货单位数量，比如出货时一箱子多少货
    private BigDecimal outUnitPerNum;

    //sku
    private String sku;

    //单价（人民币）
//    private BigDecimal priceRmb;
//
//    //总价（人民币）
//    private BigDecimal totalPriceRmb;

    //分类名称
    private String categoryName;
}