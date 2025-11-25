package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/24 23:14
 */

@Data
@TableName("ck_production_task")
public class ProductionTask extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //生产任务编号',
    private String taskNo;
    //领料出库单ID',
    private Long outboundOrderId;
    // 领料出库单号',
    private String outboundOrderNo;
    // 成品ID',
    private Long productId;
    // 成品名称',
    private String productName;
    // 计划生产数量',
    private BigDecimal plannedQuantity;
    // 领料数量',
    private BigDecimal materialQuantity;
    //  已入库数量',
    private BigDecimal producedQuantity;
    // 剩余可入库数量',
    private BigDecimal remainingQuantity;
    // 锁定数量',
    private BigDecimal lockQuantity;
    // 状态:0-生产中,1-部分完成,2-已完成,9-已取消',
    private Integer status;
    //仓库ID',
    private Long warehouseId;
    //预计完成日期',
    private Date expectedDate;
    //备注',
    private String remark;
}
