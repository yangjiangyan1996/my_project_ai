package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//库存表
@Data
@TableName("ck_inventory")
public class Inventory extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //仓库ID
    private Long warehouseId;
    //产品ID
    private Long productId;
    //当前库存数量
    private BigDecimal quantity;
    //锁定数量
    private BigDecimal lockedQuantity;
}