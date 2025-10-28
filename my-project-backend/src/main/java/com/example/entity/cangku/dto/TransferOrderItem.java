package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;


//调拨单明细表
@Data
@TableName("ck_transfer_order_item")
public class TransferOrderItem extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //调拨单ID
    private Long orderId;
    //产品ID
    private Long productId;
    //调拨数量
    private Integer quantity;
}