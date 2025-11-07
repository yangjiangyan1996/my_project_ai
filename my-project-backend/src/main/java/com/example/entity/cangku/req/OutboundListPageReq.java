package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class OutboundListPageReq extends PageReq {
    private String endDate;
    private String startDate;
    private Integer orderType;
    private String orderNo;
    private Integer status;
    private Long customerId;
    private Long warehouseId;

    Long userId;
    Long tenantId;
}
