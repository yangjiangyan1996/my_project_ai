package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:20
 */
@Data
public class SkuPageListResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //产品sku
    private String productSku;

    private String productName;

    private String spec;

    private String color;
    //客户ID
    private Long customerId;

    private String customerName;
    //客户SKU编码
    private String customerSku;
    //状态:0-禁用,1-启用
    private Integer status;
    //备注
    private String remark;

    private Date createdAt;
    private Date updatedAt;
}
