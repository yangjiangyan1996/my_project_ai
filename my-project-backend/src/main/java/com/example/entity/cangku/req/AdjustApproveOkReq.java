package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2026/1/14 18:18
 */
@Data
public class AdjustApproveOkReq {
    private Long id;
    private String approveRemark;
    private Integer approveStatus;
    private Long userId;
    private Long tenantId;
}
