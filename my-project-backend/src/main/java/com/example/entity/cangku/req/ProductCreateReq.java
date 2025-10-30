
package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class ProductCreateReq {
    private Long id;
    //sku编号
    private String sku;
    //条形码
    private String barcode;
    //商品名称
    private String name;
    //规格型号
    private String spec;
    //分类code
    private String categoryCode;
    //单位
    private String unitCode;
    //出货单位（比如按箱子出货）
    private String outUnitCode;
    //出货单位数量，比如出货时一箱子多少货
    private Long outUnitPerNum;
    //单品重量(可选)
    private Double weightPerUnit;
    //颜色
    private String color;
    //最低库存
    private Long minStock;
    // 备注
    private String remark;
    //状态：0-禁用，1-启用
    private Integer status;

    private Long tenantId;
    private Long userId;
}
