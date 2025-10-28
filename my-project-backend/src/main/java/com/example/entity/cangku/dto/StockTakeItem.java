package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//库存盘点明细表
@Data
@TableName("ck_stock_take_item")
public class StockTakeItem extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //盘点单ID
    private Long stockTakeId;
    //产品ID
    private Long productId;
    //系统库存数量
    private Integer systemQuantity;
    //实际盘点数量
    private Integer actualQuantity;
    //差异数量(实际-系统)
    private Integer difference;
}