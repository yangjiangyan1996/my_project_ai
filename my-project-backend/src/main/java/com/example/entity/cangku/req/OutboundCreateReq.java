package com.example.entity.cangku.req;

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
public class OutboundCreateReq {
    // 附件
    private List<String> attachments;
    // 客户
    private Long customerId;
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
    // 总数量
    private BigDecimal totalQuantity;
    // 仓库id
    private Long warehouseId;

    private List<ProductInfoInner> items;

    private Long userId;
    private Long tenantId;

    // 商品信息
    @Data
    public static class ProductInfoInner {
        // 商品id
        private Long productId;
        // 数量
        private BigDecimal quantity;
        // 价格
        private BigDecimal price;
        // 金额
        private BigDecimal amount;
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
        private String batchNo;
        private BigDecimal quantity;
    }

}
