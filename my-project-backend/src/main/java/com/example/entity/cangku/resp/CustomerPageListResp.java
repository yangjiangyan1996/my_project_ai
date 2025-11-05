
package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class CustomerPageListResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //客户编码
    private String customerCode;
    //客户名称
    private String customerName;
    //联系人
    private String contactPerson;
    //联系电话
    private String contactPhone;
    //地址
    private String address;
    //状态:0-禁用,1-启用
    private Integer status;
    //银行账号
    private String bankAccount;
    //开户行
    private String bankName;
    //营业执照
    private String businessLicense;
    //信用额度
    private Integer creditLimit;
    //客户等级
    private Integer customerLevel;
    //客户类型
    private Integer customerType;
    //邮箱
    private String email;
    //税号
    private String taxNumber;
    //备注
    private String remark;

    //创建时间
    private String createdAt;

}
