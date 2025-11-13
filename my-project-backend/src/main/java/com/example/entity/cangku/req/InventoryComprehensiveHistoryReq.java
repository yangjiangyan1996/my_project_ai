package com.example.entity.cangku.req;

import lombok.Data;

@Data
public class InventoryComprehensiveHistoryReq {
    private Integer page;
    private Integer size;
    private Long productId;
    private Integer orderType;
    private Long warehouseId;
    private String changeType; // 'in' or 'out'
    private String startDate;
    private String endDate;
    private String sortField;
    private String sortOrder;
    private Long tenantId;
    
    // getters and setters
}