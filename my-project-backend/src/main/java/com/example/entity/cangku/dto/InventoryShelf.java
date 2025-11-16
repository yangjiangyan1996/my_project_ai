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
 * @Date 2025/11/15 22:41
 */
@Data
@TableName("ck_inventory_shelf")

public class InventoryShelf extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品ID
    private Long productId;
    //仓库ID
    private Long warehouseId;
    //批次号
    private String batchNo;
    //货架ID
    private Long shelfId;
    //当前库存数量
    private BigDecimal quantity;
    //锁定数量
    private BigDecimal lockedQuantity;
}
