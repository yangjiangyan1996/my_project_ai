
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
public class AdjustListPageReq extends PageReq {
    Long warehouseId;
    String adjustNo;
    Integer adjustType;
    Integer sourceType;
    Integer adjustStatus;
    String startDate;
    String endDate;

    Long userId;
    Long tenantId;
}
