package com.example.entity.cangku.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存锁定DTO
 */
@Data
@Builder
public class InventoryLockDto {
    private Long tenantId;
    private Long orderId;
    private String orderNo;
    private Integer orderType;
    private Long warehouseId;
    private Long userId;
    
    private Long productId;
    private String batchNo;
    private Long shelfId;
    private BigDecimal quantity;
    
    private String lockReason;
    private String expectedDate;
}