package com.example.entity.cangku.resp;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 锁定结果响应
 */
@Data

@Builder
public class LockResult {
    private Boolean success;
    private String lockNo;
    private String message;
    private Date lockTime;
    private Date unlockTime;
    
    private Long orderId;
    private Integer lockCount;
    private Integer unlockCount;
    
    private List<LockItemDetail> lockItems;
    private List<LockItemDetail> unlockItems;
    private List<LockFailureItem> failureItems;
    
    @Data
    @Builder
    public static class LockItemDetail {
        private Long productId;
        private Long relationProductId; // 关联成品ID（生产领料用）
        private String batchNo;
        private Long shelfId;
        private BigDecimal planQuantity; // 计划数量
        private BigDecimal lockedQuantity; // 锁定/解锁数量
        private Long lockId; // 锁定记录ID
    }
    
    @Data
    @Builder
    public static class LockFailureItem {
        private Long productId;
        private String batchNo;
        private BigDecimal planQuantity;
        private String reason;
    }
}