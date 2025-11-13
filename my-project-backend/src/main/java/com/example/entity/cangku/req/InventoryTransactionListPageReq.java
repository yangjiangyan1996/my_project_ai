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
public class InventoryTransactionListPageReq extends PageReq {
//    private String changeType; // 'in' or 'out'
    private String endDate;
    private String startDate;
    private Integer orderType; // 1=入库， 2=出库
    private Long productId;
    private Long warehouseId;

    Long userId;
    Long tenantId;
}
