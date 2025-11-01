
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
public class InboundListPageReq extends PageReq {
    String startDate;
    String endDate;
    String orderNo;
    Integer orderType;
    Integer status;
    Long warehouseId;
    Long supplierId;

    Long userId;
    Long tenantId;
}
