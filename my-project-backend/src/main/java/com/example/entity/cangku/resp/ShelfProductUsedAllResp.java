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
    private Long productId;
    private String sku;
    private BigDecimal totalAllocated;
    private List<BatchAllocation> batchAllocations;

    @Data
    public static class BatchAllocation {
        private Long batchNo;
        private Date createdAtOfBatch;
        private BigDecimal totalAllocated;
        private List<ShelfAllocation> allocations;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShelfAllocation {
        private Long shelfId;
        private String shelfName;
        private BigDecimal quantity;
    }
}
