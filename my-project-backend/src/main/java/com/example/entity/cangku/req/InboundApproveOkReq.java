package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/3 00:21
 */
@Data
public class InboundApproveOkReq {
    private Long orderId;
    private Integer status;

    private Long userId;
    private Long tenantId;
}
