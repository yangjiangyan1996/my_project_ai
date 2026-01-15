package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//库存流水表
@Data
@TableName("ck_inventory_transaction")
public class InventoryTransaction extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //仓库ID
    private Long warehouseId;
    //批次号
    private String batchNo;
    //货架ID
    private Long shelfId;
    //产品ID
    private Long productId;
    //单据类型:1-入库单,2-出库单
    private Integer orderType;
    //出入库类型，具体到销售出库，采购入库表的类型
    private Integer orderTypeDetail;
    //单据ID
    private Long orderId;
    //单据明细ID
    private Long orderItemId;
    //变动数量(正数表示增加，负数表示减少)
    private BigDecimal changeQuantity;
    //变动后结存数量
    private BigDecimal balanceQuantity;
    //变动前结存数量
    private BigDecimal beforBalanceQuantity;
    //业务发生时间
    private Date transactionTime;
    //单价
    private BigDecimal priceUnit;
    //总价
    private BigDecimal priceTotal;
    //美元单价
    private BigDecimal priceUnitUsd;
    //美元总价
    private BigDecimal priceTotalUsd;
    //备注
    private String  remark;
}