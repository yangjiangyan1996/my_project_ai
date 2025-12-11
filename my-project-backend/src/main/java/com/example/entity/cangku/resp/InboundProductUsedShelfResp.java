package com.example.entity.cangku.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/11 21:53
 */
@Data
public class InboundProductUsedShelfResp {
    //商品ID
    private Long productId;

    //商品分配的货架信息
    private List<ProductUsedShelfRespInner> shelfQuantityList;

    @Data
    @AllArgsConstructor
    public static class ProductUsedShelfRespInner{
        //货架ID
        private Long shelfId;
        //分配的数量
        private BigDecimal quantity;
    }
}
