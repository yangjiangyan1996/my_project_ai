package com.example.entity.cangku.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存解锁DTO
 */
@Data
@Builder
public class InventoryUnlockDto {
    private Long tenantId;
    private Long orderId;
    private Long userId;
    private String unlockReason;
    private BigDecimal unlockQuantity; // 指定解锁数量，null表示全部解锁
}
