package com.example.entity.cangku.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:13
 */
@Data
public class CkTenantResp {
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
    //服务到期时间
    private LocalDateTime expireAt;

    //老板权限
    private boolean bossAuth;
}
