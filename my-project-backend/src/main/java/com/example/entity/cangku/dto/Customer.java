package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

@Data
@TableName("ck_customer")
public class Customer extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //客户编码
    private String code;
    //客户名称
    private String name;
    //联系人
    private String contactPerson;
    //联系电话
    private String contactPhone;
    //地址
    private String address;
    //状态:0-禁用,1-启用
    private Integer status;
}