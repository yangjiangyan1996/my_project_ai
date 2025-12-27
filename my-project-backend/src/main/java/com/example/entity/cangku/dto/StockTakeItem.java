package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:12
 */
//ck_stock_take_item
@Data
@TableName("ck_stock_take_item")
public class StockTakeItem extends BaseModel {
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

    /** 系统库存数量 */
    private BigDecimal systemQuantity;

    /** 实盘数量 */
    private BigDecimal countedQuantity;

    /** 差异数量 */
    private BigDecimal diffQuantity;

    /** 状态:1-未盘,2-已盘,3-已确认 */
    private Integer status;
}
