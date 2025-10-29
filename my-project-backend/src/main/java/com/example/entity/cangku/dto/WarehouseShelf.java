package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//仓库货架表
@Data
@TableName("ck_warehouse_shelf")
public class WarehouseShelf extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //货架编码
    private String shelfCode;
    //货架名称
    private String shelfName;
    //仓库ID
    private Long warehouseId;
    //区域（A区、B区等）
    private String area;
    //排
    private String row;
    //列
    private String column;
    //层
    private String layer;
    //容量
    private String capacity;
    //容量单位
    private String capacityUnit;
    //状态：0-禁用，1-启用
    private Integer status;
    //排序
    private Integer sortOrder;
    //备注
    private String remark;
}