package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBomExcelModel {

    //sku编号
    @ExcelProperty("sku编号")
    private String sku;

    @ExcelProperty("名称*")
    private String name;

    //规格型号
    @ExcelProperty("规格")
    private String spec;

    //颜色
    @ExcelProperty("颜色")
    private String color;

    //单位
    @ExcelProperty("单位*")
    private String unitName;

    @ExcelProperty("损耗率")
    private String lossRate;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("主料/辅料")
    private String typeName;

    @ExcelProperty("单品重量/长度")
    private String weightPerUnit;

    @ExcelProperty("最低库存")
    private String minStock;

    @ExcelProperty("备注")
    private String remark;
}
