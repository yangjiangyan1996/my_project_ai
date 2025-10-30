package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class CategoryCreateReq {
    private String categoryCode;
    private String categoryName;
    private String parentCode;
    private Integer sortOrder;
    private Integer status;

    private Long tenantId;
    private Long userId;
}
