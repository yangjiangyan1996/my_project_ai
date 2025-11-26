package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:06
 */
@Data
public class OutboundDetailResp {
    // 主键
    private Long id;
    // 附件
    private List<String> attachments;
    // 客户
    private Long customerId;
    private String customerName;
    // 预计出库时间
    private String expectedDate;
    // 订单号
    private String orderNo;
    // 订单类型
    private Integer orderType;
    // 关联订单号
    private String relatedOrderNo;
    // 备注
    private String remark;
    // 状态
    private Integer status;
    // 总金额
    private BigDecimal totalAmount;
    // 总金额（美元）
    private BigDecimal totalAmountUsd;
    // 总数量
    private BigDecimal totalQuantity;
    // 仓库id
    private Long warehouseId;

    private String warehouseName;

    private List<ProductInfoInner> items;


    private java.util.Date createdAt;
    private java.util.Date updatedAt;
    private String applicantName;
    private Long userId;
    private Long tenantId;
    private Integer itemCount;

    // 商品信息
    @Data
    public static class ProductInfoInner {
        // 商品id
        private Long productId;
        private String productName;
        private String sku;
        private String batchNo;
        private String spec;
        private String unit;
        private String color;
        // 数量
        private BigDecimal quantity;
        //单价
        private BigDecimal priceUnit;
        //总价
        private BigDecimal priceTotal;
        //美元单价
        private BigDecimal priceUnitUsd;
        //美元总价
        private BigDecimal priceTotalUsd;
        // 备注
        private String remark;


        // 批次数量
        List<ProductInventoryBatchInner> availableBatches;
        // 分配的批次数量
        List<ProductInventoryBatchInner> batchAllocations;
    }


    // 分配批次信息
    @Data
    public static class ProductInventoryBatchInner {
        private Long itemId;
        private String batchNo;
        private BigDecimal quantity;
        private Long shelfId;
        private String shelfName;
    }

}
