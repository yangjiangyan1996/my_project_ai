package com.example.entity.cangku.vo;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/9 10:25
 */
@Data
public class SaleOutBoundItemExtVO {
    private Long productId;
    //标识是否为触发产品 0=是， 1=不是
    private Integer isTriggerProduct;
    //标识是否为推荐产品  0=是， 1=不是
    private Integer isRecommendProduct;
    //关联的触发产品ID
    private Long triggerProductId;
}
