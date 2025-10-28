package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//审核流程定义表
@Data
@TableName("ck_approval_flow")
public class ApprovalFlow extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //流程名称
    private String name;
    //业务类型:1-入库单,2-出库单,3-调拨单,4-盘点单
    private Integer bizType;
    //状态:0-停用,1-启用
    private Integer status;
    //流程描述
    private String description;
}