package com.example.entity.cangku.dto;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:12
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;
@Data
@TableName("ck_customer_sku_mapping")
public class CustomerSkuMapping extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品sku
    private String productSku;
    //客户ID
    private Long customerId;
    //客户SKU编码
    private String customerSku;
    //状态:0-禁用,1-启用
    private Integer status;
    //备注
    private String remark;
}
