package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/17 17:10
 */
@Data
public class SkuCreateReq {
    private Long id;
    //客户ID
    private Long customerId;
    //客户SKU编码
    private String customerSku;
    //商品SKU编码
    private String productSku;
    //状态:0-禁用,1-启用
    private Integer status;
    //备注
    private String remark;

    //租户ID
    private Long tenantId;
    private Long userId;
}
