package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//库存表
@Data
@TableName("ck_inventory_batch")
public class InventoryBatch extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品ID
    private Long productId;
    //批次号
    private String batchNo;
    //仓库ID
    private Long warehouseId;
    //当前库存数量
    private BigDecimal quantity;
    //锁定数量
    private BigDecimal lockedQuantity;
    //来源入库单ID
    private Long inboundOrderId;
    //来源入库明细ID
    private Long inboundItemId;
    //生产日期
    private Date productionDate;
}