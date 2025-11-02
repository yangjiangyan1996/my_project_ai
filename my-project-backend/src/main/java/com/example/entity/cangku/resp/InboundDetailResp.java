
package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class InboundDetailResp {
    Long id;
    private String orderNo;
    private Integer orderType;
    private String relatedOrderNo;
    private String remark;
    private Integer status;
    private Long supplierId;
    private Double totalQuantity;
    private Long warehouseId;
    private List<InboundDetailCreateReq> items;

    @Data
    public static class InboundDetailCreateReq {
        private Double actualQuantity;
        private String batchNo;
        private String spec;
        private String unit;
        private Long productId;
        private String remark;
        private Long shelfLocationId;
    }

    private Long tenantId;
    private Long userId;
}
