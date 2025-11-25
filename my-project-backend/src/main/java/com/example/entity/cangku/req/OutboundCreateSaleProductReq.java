package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/26 00:03
 */
@Data
public class OutboundCreateSaleProductReq {
    private Long id;
    private String orderNo;
    private Integer orderType;
    private Long warehouseId;
    private Long customerId;
    private String expectedDate;
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private List<OrderItemInner> items;
    private List<Object> attachments; // 根据实际情况可以定义具体的Attachment类
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal totalAmountUsd;

    private Long userId;
    private Long tenantId;


    @Data
    public static class OrderItemInner {
        private Long productId;
        private String productName;
        private String sku;
        private String spec;
        private String unit;
        private String color;
        private BigDecimal currentStock;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal priceTotal;
        private BigDecimal priceUnitUsd;
        private BigDecimal priceTotalUsd;
        private List<BatchAllocationInner> batchAllocations;
        private String remark;
    }

    @Data
    public static class BatchAllocationInner {
        private String batchNo;
        private Long shelfId;
        private String shelfName;
        private BigDecimal quantity;
        private BigDecimal price;
    }
}
