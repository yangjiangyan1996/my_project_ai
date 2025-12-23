package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:13
 */
@Data
public class CkTenantUnauthResp {
    private Long id;
    //租户/企业名称
    private String name;

    private String image;
}
