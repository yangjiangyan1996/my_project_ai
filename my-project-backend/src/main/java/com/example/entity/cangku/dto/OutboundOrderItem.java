package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//出库单明细表
@Data
@TableName("ck_outbound_order_item")
public class OutboundOrderItem extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //出库单ID
    private Long orderId;
    //产品ID
    private Long productId;
    //计划数量
    private BigDecimal quantity;
    //实际数量
    private Integer actualQuantity;
}