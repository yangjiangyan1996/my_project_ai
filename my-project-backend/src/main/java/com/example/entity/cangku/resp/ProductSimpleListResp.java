
package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class ProductSimpleListResp {
    private Long id;
    //sku编号
    private String sku;
    //商品名称
    private String name;
    //规格型号
    private String spec;
    //颜色
    private String color;
}
