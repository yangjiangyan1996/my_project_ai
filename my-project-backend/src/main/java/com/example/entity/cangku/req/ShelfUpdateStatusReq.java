package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:21
 */
@Data
public class ShelfUpdateStatusReq {
    private Long id;
    private Integer status;
    private Long userId;
    private Long tenantId;
}
