package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//仓库表
@Data
@TableName("ck_warehouse")
public class Warehouse extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //仓库编码
    private String code;
    //仓库名称
    private String name;
    //仓库地址
    private String address;
    //负责人ID
    private Long managerId;
    //状态:0-停用,1-启用
    private Integer status;
}