package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * String @Date 2025/11/6 00;
 */
@Data
public class BomDetailAndWarehouseListResp {
    //bomdetail的ID
    private Long id;
    Long componentProductId;
    String componentProductName;
    String componentProductSku;
    String componentProductSpec;
    String componentProductColor;
    String componentProductUnit;
    BigDecimal quantity;
    //基础单位数量（多少成品用一个包装）
    BigDecimal otherQuantity;
    Integer type;
    BigDecimal lossRate;
    String remark;
    Integer sortOrder;

    List<ProductWarehouseQuantityResp> warehouseQuantityList;
}
