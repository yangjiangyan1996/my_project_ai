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
 * @Date 2025/12/9 10:16
 */
@Data
@TableName("ck_outbound_order_item_sale_ext")
public class OutboundOrderItemSaleExt extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //出库单ID
    private Long orderId;
    //出库单明细ID
    private Long orderItemId;
    //产品ID
    private Long productId;
    //是否为触发产品, 0=是。1=不是
    private Integer isTriggerProduct;
    //是否为推荐产品, 0=是。1=不是
    private Integer isRecommendProduct;
    //关联的触发产品ID
    private Long triggerProductId;
}
