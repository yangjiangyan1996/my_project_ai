package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ck_adjust_order_item")
public class AdjustOrderItem extends BaseModel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long tenantId;
    
    private Long adjustOrderId;
    
    private String adjustNo;
    
    private Long productId;
    
    private String productCode;
    
    private String productName;
    
    private String skuCode;
    
    private String specification;
    
    private String unit;
    
    private Long warehouseId;
    
    private String warehouseName;
    
    private String batchNo;
    
    private Long shelfId;
    
    private String shelfCode;
    
    private String locationCode;
    
    private Integer inventoryType;
    
    private BigDecimal beforeQuantity;
    
    private BigDecimal adjustQuantity;
    
    private BigDecimal afterQuantity;
    
    private BigDecimal unitCost;
    
    private BigDecimal adjustCostAmount;
    
    private BigDecimal unitPrice;
    
    private BigDecimal adjustAmount;
    
    private String adjustReason;
    
    private String itemRemark;
    
    private Integer status;
    
    private Date executeTime;
    
    private Long executeBy;
    
    @TableField("is_affect_cost")
    private Boolean isAffectCost;
    
    private Integer costAdjustMethod;
    
    private Long sourceItemId;
    
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String extData;
}