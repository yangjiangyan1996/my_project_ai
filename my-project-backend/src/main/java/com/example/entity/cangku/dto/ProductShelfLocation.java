package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//产品货架位置表
@Data
@TableName("ck_product_shelf_location")
public class ProductShelfLocation extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品ID
    private Long productId;
    //仓库ID
    private Long warehouseId;
    //货架ID
    private Long shelfId;
    //具体位置编码
    private String locationCode;
    //最大存放数量
    private Double maxQuantity;
    //当前数量
    private Double currentQuantity;
    //安全库存数量
    private Double safetyQuantity;
    //是否主位置：0-否，1-是
    private Integer isPrimary;
    //状态：0-禁用，1-启用
    private Integer status;
}