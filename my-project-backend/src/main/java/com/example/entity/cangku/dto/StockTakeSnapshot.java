package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存盘点快照表
 */
@Data
@TableName("ck_stock_take_snapshot")
public class StockTakeSnapshot extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 盘点单ID */
    private Long stockTakeId;

    /** 产品ID */
    private Long productId;

    /** 仓库ID */
    private Long warehouseId;

    /** 批次号 */
    private String batchNo;

    /** 货架ID */
    private Long shelfId;

    /** 库位编码 */
    private String locationCode;

    /** 快照库存数量 */
    private BigDecimal snapshotQuantity;

    /** 快照时间 */
    private Date snapshotTime;
}