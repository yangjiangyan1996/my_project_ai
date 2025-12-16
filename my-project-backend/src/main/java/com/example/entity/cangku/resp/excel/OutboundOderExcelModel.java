package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/17 23:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboundOderExcelModel {
    @ExcelProperty("单号")
    private String orderNumber;

    @ExcelProperty("货架号")
    private String shelfName;

    @ExcelProperty("英文名称")
    private String englishName;

    @ExcelProperty("sku")
    private String sku;

    @ExcelProperty("客户sku")
    private String customerSku;

    @ExcelProperty("商品名称")
    private String name;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("颜色")
    private String color;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("每箱数量")
    private String outUnitPerNum;

    @ExcelProperty("箱数")
    private String boxCount;

    @ExcelProperty("出货单位长度")
    private String outUnitLength;

    @ExcelProperty("出货单位宽度")
    private String outUnitWidth;

    @ExcelProperty("出货单位高度")
    private String outUnitHeight;

    @ExcelProperty("体积(m3)")
    private String volume;

    @ExcelProperty("单件重量(kg)")
    private String weightPerUnit;

    @ExcelProperty("总重量")
    private String weightAll;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("价格(人民币)")
    private String priceUnit;

    @ExcelProperty("总价(人民币)")
    private String priceTotal;

    @ExcelProperty("价格(美元)")
    private String priceUnitUsd;

    @ExcelProperty("总价(美元)")
    private String priceTotalUsd;
}
