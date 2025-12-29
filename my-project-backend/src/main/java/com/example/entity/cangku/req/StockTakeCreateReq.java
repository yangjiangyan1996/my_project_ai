package com.example.entity.cangku.req;

import lombok.Data;

import java.util.List;

@Data
public class StockTakeCreateReq {
    private Long warehouseId;
    // 1动态 2静态
    private Integer takeType;
    //  1: '全部', 2: '批次',  3: '货架', 4: '商品'
    private Integer takeScope;

    /**
     * takeScope 对应几，下面的集合就有数据
     */
    // 货架id
    List<String> shelfIds;
    // 批次id
    List<String> batchIds;
    // 商品id
    List<String> productIds;

    private String remark;

    private Long tenantId;
    private Long userId;
}
