package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:16
 */
@Data
public class CustomerEnabledListResp {
    private Long id;
    //客户编码
    private String customerCode;
    //客户名称
    private String customerName;
    //客户等级
    private Integer customerLevel;
    private String customerLevelName;
    //客户类型
    private Integer customerType;
    private String customerTypeName;
    //备注
    private String remark;
}
