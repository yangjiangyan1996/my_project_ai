
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
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private Long supplierId;
    private Double totalQuantity;
    private Long warehouseId;
    private List<InboundDetailCreateReq> items;
    //总价
    private BigDecimal totalAmount;

    @Data
    public static class InboundDetailCreateReq {
        private Double actualQuantity;
        private String batchNo;
        private Long productId;
        private String remark;
        private String shelfLocationId;
        //单价
        private BigDecimal priceUnit;
        //总价
        private BigDecimal priceTotal;
    }

    private Long tenantId;
    private Long userId;
}
