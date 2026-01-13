package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

//仓库货架表
@Data
public class ShelfZoneResp {
//    //货架编码
    private String zoneCode;
//    //货架名称
    private String zoneName;
    private Long id;
    private Long tenantId;
//    private String zoneCode;
//    private String zoneName;
    private Long warehouseId;
    private String area;
    private String rowN;
    private String columnN;
    private String layer;
    private BigDecimal capacity;
    private String capacityUnit;
    private Integer status;
    private Integer sortOrder;
    private String remark;
    private Integer zoneLevel;
    private Long parentId;
}