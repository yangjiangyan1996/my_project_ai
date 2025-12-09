package com.example.entity.cangku.req;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:21
 */
@Data
public class OutboundExportSaleProductReq {
    private Long warehouseId;
    private List<Long> productIds;
}
