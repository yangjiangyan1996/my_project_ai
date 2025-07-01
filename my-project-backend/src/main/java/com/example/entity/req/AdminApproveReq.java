package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/1 14:44
 */
@Data
public class AdminApproveReq {
    private Long projectId;
    private String reason;
}
