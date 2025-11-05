package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//入库单明细表
@Data
@TableName("ck_inbound_order_item")
public class InboundOrderItem extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //入库单ID
    private Long orderId;
    //产品ID
    private Long productId;
    //实际数量
    private BigDecimal actualQuantity;
    //货架位置ID
    private Long shelfLocationId;
    //具体位置编码
    private String locationCode;
    //批次号
    private String batchNo;
    //备注
    private String remark;
    //单价
    private BigDecimal price;
    //总价
    private BigDecimal totalPrice;
}