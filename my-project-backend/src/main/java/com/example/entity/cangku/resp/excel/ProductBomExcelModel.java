package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBomExcelModel {
    //sku编号
    @ExcelProperty("sku编号")
    private String sku;

    @ExcelProperty("主料/辅料")
    private String typeName;

    @ExcelProperty("名称*")
    private String name;

    @ExcelProperty("备注")
    private String remark;

    //规格型号
    @ExcelProperty("规格")
    private String spec;

    //颜色
    @ExcelProperty("颜色")
    private String color;

    //基础单位数量（多少成品用一个包装）
    @ExcelProperty("数量")
    private String otherQuantity;

    //生产一个成品所需的原料净用量数量（目前只存储，无逻辑）
    @ExcelProperty("净用量")
    private BigDecimal quantityBeforeLoss;

    @ExcelProperty("单位名称")
    private String unitName;


    @ExcelProperty("损耗率(%)")
    private String lossRate;

    @ExcelProperty("总用量")
    private String quantity;

    @ExcelProperty("单品重量/长度")
    private String weightPerUnit;

    @ExcelProperty("最低库存")
    private String minStock;
}
