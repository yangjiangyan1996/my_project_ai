package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 10:29
 */
@Data
@TableName("ck_product_image")
public class ProductImage  extends BaseModel {
    /**
     * 主键ID，自增类型
     */

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long productId;
    /**
     * {@link com.example.enums.CkProductEnums.ProductImageType}
     */
    private Integer productImageType;
    private String image;

}
