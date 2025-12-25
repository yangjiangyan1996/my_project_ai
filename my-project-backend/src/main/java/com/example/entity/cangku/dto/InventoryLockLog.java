package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ck_inventory_lock_log")
public class InventoryLockLog extends BaseModel {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long tenantId;
    private Long lockId;
    
    private Integer operationType; // 操作类型
    private String operationSource; // 操作来源
    private String operationReason; // 操作原因
    
    private BigDecimal beforeLockQuantity; // 操作前锁定数量
    private BigDecimal beforeUnlockQuantity; // 操作前已解锁数量
    private BigDecimal changeLockQuantity; // 锁定数量变化
    private BigDecimal changeUnlockQuantity; // 解锁数量变化
    
    private Integer beforeLockStatus; // 操作前锁定状态
    private Integer afterLockStatus; // 操作后锁定状态
    
    private Long relatedOrderId; // 关联单据ID
    private Integer relatedOrderType; // 关联单据类型
    
    private String extData; // 扩展数据
}