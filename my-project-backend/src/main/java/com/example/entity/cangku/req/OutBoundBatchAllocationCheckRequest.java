package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OutBoundBatchAllocationCheckRequest {
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 当前操作的商品项索引（用于排除自身）
     */
    private Long currentProductId;
    
    /**
     * 所有商品项的分配数据
     */
    private List<ProductAllocationDTO> productAllocations;
    
    @Data
    public static class ProductAllocationDTO {
        /**
         * 成品商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品数量
         */
        private BigDecimal productQuantity;
        
        /**
         * BOM分配数据
         */
        private List<BomAllocationDTO> bomAllocations;
    }
    
    @Data
    public static class BomAllocationDTO {
        /**
         * 原料商品ID
         */
        private Long componentProductId;

        /**
         * 原料商品名称
         */
        private String componentProductName;
        
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

        /**
         * 分配数量
         */
        private BigDecimal quantity;

        /**
         * 新增
         * 总需求量 ：用来控制分配的最高数量
         */
        private BigDecimal totalBomQuantity;
    }
}