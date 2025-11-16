package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/15 23:08
 */
@Data
public class ProductWarehouseQuantityResp {
    //仓库ID
    private Long warehouseId;
    //仓库名称
    private String warehouseName;
    //仓库库存
    private BigDecimal warehouseQuantity;
    //仓库可用库存
    private BigDecimal warehouseAvailableQuantity;
    //货架数量列表
    List<ProductShelfQuantityResp> shelfQuantityList;
}
