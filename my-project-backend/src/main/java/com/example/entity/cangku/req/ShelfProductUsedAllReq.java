package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/11 22:48
 */
@Data
public class ShelfProductUsedAllReq {
    //表格序号
    private Integer index;
    //商品ID
    private Long productId;
    //商品SKU
    private String sku;
    //商品需求总量
    private BigDecimal quantity;

    //批次列表
    private List<ShelfProductUsedAllOfPcReq> pcList;


    @Data
    public static class ShelfProductUsedAllOfPcReq {
        //批次ID
        private String batNo;
        //批次创建时间
        private Date createdAtOfBatch;
        //货架可用数量列表
        private List<ShelfProductUsedAllOfShelfQuantityReq> shelfQuantityList;
    }

    @Data
    public static class ShelfProductUsedAllOfShelfQuantityReq {
        //货架ID
        private Long shelfId;
        //货架名称
        private String shelfName;
        //货架可用数量
        private BigDecimal quantity;
    }
}