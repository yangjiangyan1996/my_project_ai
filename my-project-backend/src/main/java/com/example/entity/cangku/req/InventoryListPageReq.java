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
public class InventoryListPageReq extends PageReq {
    private String categoryId;
    private String productCode;
    private String productName;
    private String sortField;
    private String stockStatus;
    private String warehouseId;
    private String sortOrder;

    Long userId;
    Long tenantId;
}
