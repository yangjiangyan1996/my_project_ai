
package com.example.entity.cangku.resp;

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
public class InboundDetailResp {
    Long id;
    private String orderNo;
    private Integer orderType;
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private Long supplierId;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private Long warehouseId;
    private String warehouseName;
    private String supplierName;
    private java.util.Date createdAt;
    private java.util.Date modifiedAt;
    private List<InboundDetailCreateReq> items;
    private Integer itemCount;

    @Data
    public static class InboundDetailCreateReq {
        private Long itemId;
        private BigDecimal actualQuantity;
        private String batchNo;
        private String spec;
        private String unit;
        private Long productId;
        private String productName;
        private String sku;
        private String remark;
        private Long shelfLocationId;
        private String shelfLocationName;
        private BigDecimal priceUnit;
        private BigDecimal priceTotal;
    }

    private Long tenantId;
    private Long userId;
}
