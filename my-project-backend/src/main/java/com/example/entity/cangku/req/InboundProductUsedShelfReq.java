package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/11 22:48
 */
@Data
public class InboundProductUsedShelfReq {
    //仓库ID
    private Long warehouseId;

    //商品分配的数量信息
    private List<InboundProductUsedShelfInner> list;

    private Long userId;
    private Long tenantId;

    @Data
    public static class InboundProductUsedShelfInner {
        //商品ID
        private Long productId;
        //商品SKU
        private String sku;
        //需要分配的数量
        private BigDecimal quantity;
    }
}
