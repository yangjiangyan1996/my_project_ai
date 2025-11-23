package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OutBoundBatchAllocationCheckResponse {
    /**
     * 检查结果
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 成品分配信息
     */
    private List<ProductParentDto> batchAllocatedList;

    //成品信息
    @Data
    public static class ProductParentDto {
        private Long productParentId;
        private String productParentName;
        private List<ProductSonDto> productSonDtoList;
    }

    //原料信息
    @Data
    public static class ProductSonDto {
        private Long productParentId;
        private String productParentName;

        private Long productSonId;
        private String productSonName;
        private List<BatchCountDTO> batchList;
    }


    @Data
    public static class BatchCountDTO {
        private Long productParentId;
        private String productParentName;

        private Long productSonId;
        private String productSonName;

        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 批次可用数量
         */
        private BigDecimal availableBatchQuantity;

        /**
         * 批次货架可用数量信息
         */
        private List<BatchShelfAvailableDTO> shelfList;
    }

    @Data
    public static class BatchShelfAvailableDTO {
        private Long productParentId;
        private String productParentName;
        private Long productSonId;
        private String productSonName;
        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 货架ID
         */
        private Long shelfId;
        /**
         * 货架名称
         */
        private String shelfName;
        //货架可用数量
        private BigDecimal shelfAvailableQuantity;
        //已分配数量
        private BigDecimal allocatedQuantity;
    }
}