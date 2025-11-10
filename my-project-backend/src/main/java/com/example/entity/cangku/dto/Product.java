package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

//产品表
@Data
@TableName("ck_product")
public class Product extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //sku编号
    private String sku;
    //条形码
    private String barcode;
    //商品名称
    private String name;
    //规格型号
    private String spec;
    //分类code
    private String categoryCode;
    //单位
    private String unitCode;
    //出货单位（比如按箱子出货）
    private String outUnitCode;
    //出货单位数量，比如出货时一箱子多少货
    private BigDecimal outUnitPerNum;
    //单品重量(可选)
    private BigDecimal weightPerUnit;
    //颜色
    private String color;
    //最低库存
    private Long minStock;
    // 备注
    private String remark;
    //状态：0-禁用，1-启用
    private Integer status;

    //出货单位的体积：高度
    private  BigDecimal outUnitHeight;
    //出货单位的体积：长度
    private  BigDecimal outUnitLength;
    //出货单位的体积：宽度
    private  BigDecimal outUnitWidth;
}