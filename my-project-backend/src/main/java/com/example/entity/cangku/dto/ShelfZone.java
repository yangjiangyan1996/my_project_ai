package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//仓库货架表
@Data
@TableName("ck_shelf_zone")
public class ShelfZone extends BaseModel {
    @TableId(type = IdType.AUTO)
//    private Long id;
//    //租户ID
//    private Long tenantId;
//    //货架编码
    private String shelfCode;
//    //货架名称
    private String shelfName;
//    //仓库ID
//    private Long warehouseId;
//    //区域（A区、B区等）
//    private String area;
//    //排
//    private String rowN;
//    //列
//    private String columnN;
//    //层
//    private String layer;
//    //容量
//    private double capacity;
//    //容量单位
//    private String capacityUnit;
//    //状态：0-禁用，1-启用
//    private Integer status;
//    //排序
//    private Integer sortOrder;
//    //备注
//    private String remark;
//
    //  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
//  `zone_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区域编码',
//  `zone_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区域名称',
//  `warehouse_id` bigint DEFAULT NULL COMMENT '仓库ID',
//  `area` varchar(50) DEFAULT NULL COMMENT '区域（A区、B区等）',
//  `row_n` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '排',
//  `column_n` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列',
//  `layer` varchar(20) DEFAULT NULL COMMENT '层',
//  `capacity` decimal(10,2) DEFAULT NULL COMMENT '容量',
//  `capacity_unit` varchar(20) DEFAULT NULL COMMENT '容量单位',
//  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
//  `sort_order` int DEFAULT '0' COMMENT '排序',
//  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
//  `created_by` bigint NOT NULL DEFAULT '0' COMMENT '创建人用户ID',
//  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
//  `modified_by` bigint NOT NULL DEFAULT '0' COMMENT '修改人',
//  `modified_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
//  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除，0:正常;1:被删除;',
//  `parent_id` bigint DEFAULT NULL COMMENT '父货架ID，如果为NULL表示自身就是货架',
//  `zone_level` tinyint DEFAULT '1' COMMENT '区域层级：1-货架，2-子区域',
    private Long id;
    private Long tenantId;
//    private String zoneCode;
//    private String zoneName;
    private Long warehouseId;
    private String area;
    private String rowN;
    private String columnN;
    private String layer;
    private BigDecimal capacity;
    private String capacityUnit;
    private Integer status;
    private Integer sortOrder;
    private String remark;
    private Integer zoneLevel;
    private Long parentId;
}