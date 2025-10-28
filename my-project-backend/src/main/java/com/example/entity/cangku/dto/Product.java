package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//产品表
@Data
@TableName("ck_product")
public class Product extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品SKU
    private String sku;
    //产品名称
    private String name;
    //规格型号
    private String spec;
    //单位
    private String unit;
    //分类
    private String category;
    //最低库存
    private Integer minStock;
    //最高库存
    private Integer maxStock;
}