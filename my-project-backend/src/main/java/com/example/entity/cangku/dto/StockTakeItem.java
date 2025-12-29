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

    /** 状态:1-待盘点,2-已初盘,3-已复盘,4-已确认*/
    private Integer status;

    //快照数量
    private BigDecimal snapshotQuantity;
    //版本号
    private Integer version;
    //调整状态:0-未调整,1-调整中,2-已调整
    private Integer adjustStatus;
    //初盘数量
    private BigDecimal firstCountQuantity;
    //复盘数量
    private BigDecimal secondCountQuantity;
    //三盘数量
    private BigDecimal thirdCountQuantity;
    //盘点次数
    private Integer countTimes;
    //盘点方式:1-人工,2-PDA,3-RFID
    private Integer countType;

}
