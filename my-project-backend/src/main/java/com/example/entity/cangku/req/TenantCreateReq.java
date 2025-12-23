package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:21
 */
@Data
public class TenantCreateReq {
    private Long id;
    //租户/企业名称
    private String name;

    private String image;
    //状态:0-禁用,1-启用
    private Integer status;
    //联系人
    private String contactPerson;
    //联系电话
    private String contactPhone;

    private Long userId;
    private Long tenantId;
}
