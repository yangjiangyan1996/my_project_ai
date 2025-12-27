package com.example.entity.cangku.req;

import lombok.Data;

@Data
public class StockTakeCreateReq {
    private Long warehouseId;
    // 1动态 2静态
    private Integer takeType;
    // 1全库
    private Integer takeScope;
    private String remark;

    private Long tenantId;
    private Long userId;
}
