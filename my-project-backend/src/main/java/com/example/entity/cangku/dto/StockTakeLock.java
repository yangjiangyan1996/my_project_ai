package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.util.Date;

/**
 * 库存盘点锁定表
 */
@Data
@TableName("ck_stock_take_lock")
public class StockTakeLock extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 盘点单ID */
    private Long stockTakeId;
    
    /** 锁定范围:1: '全部', 2: '批次',  3: '货架', 4: '商品' */
    private Integer lockScope;
    
    /** 仓库ID */
    private Long warehouseId;
    
    /** 产品ID */
    private Long productId;
    
    /** 库位编码 */
    private String locationCode;
    
    /** 锁定模式:1-软锁,2-硬锁 */
    private Integer lockMode;
    
    /** 状态:1-锁定中,2-已释放 */
    private Integer lockStatus;
    
    /** 锁定时间 */
    private Date lockTime;
    
    /** 解锁时间 */
    private Date unlockTime;
}