

package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class SupplierCreateReq {
    private Long id;
    //供应商编码
    private String supplierCode;
    //供应商名称
    private String supplierName;
    //供应商类型
    private Integer supplierType;
    //联系人
    private String contactPerson;
    //联系电话
    private String contactPhone;
    //地址
    private String address;
    //状态:0-禁用,1-启用
    private Integer status;
    private String bankAccount;
    private String bankName;
    private String businessLicense;
    private String cooperationStatus;
    private String email;
    private String remark;

    private Long tenantId;
    private Long userId;
}
