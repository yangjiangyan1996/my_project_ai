package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//产品表
@Data
@TableName("ck_product_bom")
public class ProductBom extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //成品ID（对应 ck_product.id）
    private Long productId;
    //BOM编号
    private String bomCode;
    //版本号（例如V1.0，用于版本管理）
    private String version;
    //状态：0-禁用，1-启用
    private Integer status;
    //备注说明，例如工艺或用途说明
    private String remark;
}