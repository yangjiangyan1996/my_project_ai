package com.example.entity.cangku.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//入库单主表
@Data
@TableName("ck_inbound_order")
public class InboundOrder extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //入库单号
    private String orderNo;
    //关联单号
    private String relatedOrderNo;
    //仓库ID
    private Long warehouseId;
    //供应商ID
    private Long supplierId;
    //入库类型:1-采购入库,2-生产入库,3-退货入库,4-调拨入库
    private Integer orderType;
    //状态:0-待提交,1-审核中,2-已通过,3-已完成,4-已拒绝,9-已取消
    private Integer status;
    //总数量
    private BigDecimal totalQuantity;
    //总金额
    private BigDecimal totalAmount;
    //备注
    private String remark;
    //预计入库时间
    private Date expectedDate;

}