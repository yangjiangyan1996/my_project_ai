package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InventoryComprehensiveHistoryResp {
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private String color;
    private String spec;
    private String unitName;
    private Integer orderType;
    private Long orderId;
    private Long orderItemId;
    private BigDecimal changeQuantity;
    private BigDecimal balanceQuantity;
    //变动前结存数量
    private BigDecimal beforBalanceQuantity;
    private Date transactionTime;
    private BigDecimal priceUnit;
    private BigDecimal priceTotal;
    private String orderNo;
    private String batchNo;
    private Boolean isUrgent;
    private String operatorName;
    private String operatorAvatar;
    
    // 四张表关联数据
    private BigDecimal totalInventory;        // ck_inventory 总库存
    private BigDecimal warehouseInventory;    // ck_inventory_warehouse 仓库库存
    private BigDecimal batchInventory;        // ck_inventory_batch 批次库存
    private BigDecimal currentBatchQuantity;  // 当前批次数量
    
    // getters and setters
}