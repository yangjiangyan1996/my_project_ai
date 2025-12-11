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
    //仓库ID
    private Long warehouseId;

    //商品分配的数量信息
    private List<ShelfProductUsedAllReqInner> list;

    private Long userId;
    private Long tenantId;

    @Data
    public static class ShelfProductUsedAllReqInner {
        //商品ID
        private Long productId;
        //商品SKU
        private String sku;
        //需要分配的数量
        private BigDecimal quantity;

        //批次列表
        private List<ShelfProductUsedAllOfPcReq> pcList;
    }

    @Data
    public static class ShelfProductUsedAllOfPcReq {
        //批次ID
        private Long batNo;
        //批次创建时间
        private Date createdAtOfBatch;
        //批次使用数量
        private  List<ShelfProductUsedAllOfShelfQuantityReq>  shelfQuantityList;
    }

    @Data
    public static class ShelfProductUsedAllOfShelfQuantityReq {
        //货架ID
        private Long shelfId;
        //货架名称
        private String shelfName;
        //货架使用数量
        private BigDecimal quantity;
    }
}
