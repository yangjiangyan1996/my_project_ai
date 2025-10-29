package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:21
 */
@Data
public class ShelfCreateReq {
    private Long id;
    private String area;
    private double capacity;
    private String capacityUnit;
    private String columnN;
    private String layer;
    private String remark;
    private String rowN;
    private String shelfCode;
    private String shelfName;
    private Integer sortOrder;
    private Integer status;
    private Long warehouseId;

    private Long userId;
    private Long tenantId;
}
