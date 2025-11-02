package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/2 01:13
 */
@Data
public class InboundListPageResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //入库单号
    private String orderNo;

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
    //备注
    private String remark;
    private String warehouseName;
    private String supplierName;
    private Integer itemCount;
}
