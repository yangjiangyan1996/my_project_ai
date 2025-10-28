package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

@Data
@TableName("ck_role")
public class Role extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //角色编码
    private String code;
    //角色名称
    private String name;
    //角色类型:1-系统角色,2-自定义角色
    private Integer type;
    //状态:0-禁用,1-启用
    private Integer status;
    //描述
    private String description;
}