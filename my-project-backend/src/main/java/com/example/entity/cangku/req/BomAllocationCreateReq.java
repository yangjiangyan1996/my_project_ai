package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BomAllocationCreateReq {
    //items集合中的索引
    Integer itemIndex;
    Long componentProductId;
    String componentProductName;
    String componentProductSku;
    BigDecimal quantity;
    //货架ID 可能为空
    Long shelfId;
    String shelfName;
    Long warehouseId;
    String warehouseName;

}