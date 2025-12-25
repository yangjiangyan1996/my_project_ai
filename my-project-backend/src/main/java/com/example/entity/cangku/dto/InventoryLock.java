package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ck_inventory_lock")
public class InventoryLock extends BaseModel {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long tenantId;
    private Integer lockType; // 锁定类型
    private String lockSource; // 锁定来源
    private Long sourceId; // 来源单据ID
    private String sourceNo; // 来源单据号
    private Long sourceItemId; // 来源明细ID
    
    private Long productId;
    private Long warehouseId;
    private String batchNo;
    private Long shelfId;
    
    private BigDecimal lockQuantity; // 锁定数量
    private BigDecimal unlockQuantity; // 已解锁数量
    
    @TableField(exist = false)
    private BigDecimal availableLockQuantity; // 剩余锁定数量
    
    private Integer lockStatus; // 锁定状态
    private Integer lockStrategy; // 锁定策略
    
    private Integer lockPurpose; // 锁定用途
    private Integer lockDirection; // 锁定方向
    
    private Date expectedUnlockTime; // 预期解锁时间
    private Date actualUnlockTime; // 实际解锁时间
    private Date expireTime; // 过期时间
    
    private Integer priority; // 优先级
    private Integer isPreemptable; // 是否可被抢占
    
    private String lockReason; // 锁定原因
    private String extData; // 扩展数据
}