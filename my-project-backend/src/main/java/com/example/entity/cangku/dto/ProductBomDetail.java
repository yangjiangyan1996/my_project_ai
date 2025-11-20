package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//产品表
@Data
@TableName("ck_product_bom_detail")
public class ProductBomDetail extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;

    //BOM主表ID（关联 ck_product_bom.id）
    private Long bomId;

    //原料产品ID（对应 ck_product.id）
    private Long componentProductId;

    //生产一个成品所需的原料数量
    private BigDecimal quantity;

    //损耗率（百分比）
    private BigDecimal lossRate;

    //备注，比如原料替代说明
    private String remark;

    //顺序
    private Integer sortOrder;

    //1=主料，2=辅料,
    private Integer type;
}