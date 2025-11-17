package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuExcelModel {

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("颜色")
    private String color;

    @ExcelProperty("我的SKU")
    private String mySku;

    @ExcelProperty("用户SKU")
    private String userSku;
}
