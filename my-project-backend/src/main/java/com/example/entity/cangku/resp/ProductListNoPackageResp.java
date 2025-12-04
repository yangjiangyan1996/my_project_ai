
package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class ProductListNoPackageResp {
    private Long id;
    //sku编号
    private String sku;
    //条形码
    private String barcode;
    //商品名称
    private String name;
    //英文名称
    private String englishName;
    //规格型号
    private String spec;
    //分类code
    private String categoryCode;
    private String categoryName;
    //单位
    private String unitCode;
    private String unitName;
    //出货单位（比如按箱子出货）
    private String outUnitCode;
    private String outUnitName;
    //出货单位数量，比如出货时一箱子多少货
    private BigDecimal outUnitPerNum;
    //单品重量(可选)
    private BigDecimal weightPerUnit;
    //颜色
    private String color;
    //最低库存
    private Long minStock;
    // 备注
    private String remark;
    //状态：0-禁用，1-启用
    private Integer status;

    //出货单位的体积：高度
    private BigDecimal outUnitHeight;
    //出货单位的体积：长度
    private BigDecimal outUnitLength;
    //出货单位的体积：宽度
    private BigDecimal outUnitWidth;

    List<BomDetailAndWarehouseListNoPackageResp> bomData;

}
