package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class SupplierListPageReq extends PageReq {
    private String contactPerson;
    private String supplierCode;
    private String supplierName;
    private Integer status;

    Long userId;
    Long tenantId;
}
