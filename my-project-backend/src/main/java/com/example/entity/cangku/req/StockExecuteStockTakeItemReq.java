package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/27 12:27
 */
@Data
public class StockExecuteStockTakeItemReq {
    Long stockTakeId;
    Long stockTakeItemId;
    Long productId;
    BigDecimal countedQuantity;

    Long userId;
    Long tenantId;
}
