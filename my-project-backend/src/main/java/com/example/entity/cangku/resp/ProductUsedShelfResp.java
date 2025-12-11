package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/11 21:53
 */
@Data
public class ProductUsedShelfResp {
    //商品ID
    private Long productId;

    //商品分配的货架信息
    private List<Long> shelfIds;
}
