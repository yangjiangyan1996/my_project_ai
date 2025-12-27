package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * 库存盘点操作日志表
 */
@Data
@TableName("ck_stock_take_log")
public class StockTakeLog extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 盘点单ID */
    private Long stockTakeId;
    
    /** 操作类型 */
    private String actionType;
    
    /** 操作描述 */
    private String actionDesc;
}