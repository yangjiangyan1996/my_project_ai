
package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class InboundCreateReq {
    Long id;
    private String orderNo;
    private Integer orderType;
    //关联单号
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private Long supplierId;
    private Double totalQuantity;
    private Long warehouseId;
    //总价
    private BigDecimal totalAmount;
    //产品明细
    private List<InboundDetailCreateReq> items;

    private Long tenantId;
    private Long userId;

    @Data
    public static class InboundDetailCreateReq {
        private Double actualQuantity;
        //批次号
        private String batchNo;
        private Long productId;
        private String remark;
        private String shelfLocationId;
        //单价
        private BigDecimal priceUnit;
        //总价
        private BigDecimal priceTotal;

        //货架分配
        private List<ShelfDetailCreateReq> shelfAllocations;
    }



    @Data
    public static class ShelfDetailCreateReq {
        private Long shelfLocationId;
        private Double quantity;
    }
}
