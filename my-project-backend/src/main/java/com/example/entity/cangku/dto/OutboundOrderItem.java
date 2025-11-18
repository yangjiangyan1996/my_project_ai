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
    //货架位置ID
    private Long shelfLocationId;
    //关联产品ID （生产领料时，ID为成品ID）
    private Long relationProductId;
    //批次 ID
    String batchNo;
    //备注
    String remark;
    //计划数量
    private BigDecimal quantity;
    //单价
    private BigDecimal priceUnit;
    //总价
    private BigDecimal priceTotal;
    //美元单价
    private BigDecimal priceUnitUsd;
    //美元总价
    private BigDecimal priceTotalUsd;
}