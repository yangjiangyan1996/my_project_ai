package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/14 00:36
 */
@Data
public class InventoryProductDetailResp {
    private Long productId;
    //当前总库存
    private BigDecimal currentStock;
    //总记录数
    private BigDecimal totalCount;
    ////总入库数量
    private BigDecimal inQuantity;
    //总出库数量
    private BigDecimal outQuantity;
    //净变化
    private BigDecimal netChange;
}
