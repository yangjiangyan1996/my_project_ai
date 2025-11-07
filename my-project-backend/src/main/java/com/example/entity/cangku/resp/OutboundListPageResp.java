package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:42
 */
@Data
public class OutboundListPageResp {
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

    String customerName;
    String warehouseName;
    Integer itemCount;

    //申请人
    private String applicantName;
    //申请人头像
    private String applicantAvatar;

}
