package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//出库单主表
@Data
@TableName("ck_outbound_order")
public class OutboundOrder extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //出库单号
    private String orderNo;
    //关联单号
    String relatedOrderNo;
    //仓库ID
    private Long warehouseId;
    //客户ID
    private Long customerId;
    //出库类型:1-销售出库,2-生产领料,3-退货出库,4-调拨出库
    private Integer orderType;
    //状态:0-待提交,1-审核中,2-已通过,3-已完成,4-已拒绝,9-已取消
    private Integer status;
    //总数量
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    //备注
    private String remark;

    private Date expectedDate;

}