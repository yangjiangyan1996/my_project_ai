package com.example.entity.cangku.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//库存流水表
@Data
public class InventoryTransactionPageResp {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //仓库ID
    private Long warehouseId;
    //仓库名称
    private String warehouseName;
    //产品ID
    private Long productId;
    //产品名称
    private String productName;
    //颜色
    private String color;
    //规格
    private String spec;
    //单位
    private String unitName;
    //单据类型:1-入库单,2-出库单
    private Integer orderType;
    //单据ID
    private Long orderId;
    //单据明细ID
    private Long orderItemId;
    //变动数量(正数表示增加，负数表示减少)
    private BigDecimal changeQuantity;
    //变动后结存数量
    private BigDecimal balanceQuantity;
    //业务发生时间
    private Date transactionTime;
    //单价
    private BigDecimal priceUnit;
    //总价
    private BigDecimal priceTotal;

    private String orderNo;

}