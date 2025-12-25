
package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class OperationLogPageListResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //操作用户ID
    private Long userId;
    //操作模块
    private String module;
    //操作类型
    private String operation;
    //操作目标ID
    private Long targetId;
    //操作描述
    private String description;
    //IP地址
    private String ipAddress;
    //用户代理
    private String userAgent;

    private String requestParams;

    private String statusMessage;     // 状态描述

    private Date createdAt;

}
