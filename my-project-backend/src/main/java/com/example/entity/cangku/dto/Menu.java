package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

@Data
@TableName("ck_menu")
public class Menu extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //菜单名称
    private String name;
    //菜单编码
    private String code;
    //父菜单ID
    private Long parentId;
    //菜单类型:1-目录,2-菜单,3-按钮
    private Integer type;
    //路由路径
    private String path;
    //组件路径
    private String component;
    //图标
    private String icon;
    //排序
    private Integer sortOrder;
    //状态:0-禁用,1-启用
    private Integer status;
}