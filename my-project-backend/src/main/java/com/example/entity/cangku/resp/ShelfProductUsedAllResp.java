package com.example.entity.cangku.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/11 21:53
 */
@Data
public class ShelfProductUsedAllResp {
    //表格序号
    private Integer index;
    //商品ID
    private Long productId;
    //商品sku
    private String sku;
    //批次分配
    private List<BatchAllocation> batchAllocations;

    @Data
    public static class BatchAllocation {
        //批次号
        private String batchNo;
        //批次创建时间
        private Date createdAtOfBatch;
        //货架分配
        private List<ShelfAllocation> allocations;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShelfAllocation {
        //货架ID
        private Long shelfId;
        //货架名称
        private String shelfName;
        //分配数量
        private BigDecimal quantity;
    }
}
