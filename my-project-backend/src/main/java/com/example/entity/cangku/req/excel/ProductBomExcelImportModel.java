package com.example.entity.cangku.req.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBomExcelImportModel {

    //sku编号
    @ExcelProperty(value = "sku编号", index = 0)
    private String sku;

    @ExcelProperty(value = "名称*", index = 1)
    @NotBlank(message = "名称不能为空")
    private String name;

    //规格型号
    @ExcelProperty(value = "规格", index = 2)
    private String spec;

    //颜色
    @ExcelProperty(value = "颜色", index = 3)
    private String color;

    //单位
    @ExcelProperty(value = "单位*", index = 4)
    @NotBlank(message = "单位不能为空")
    private String unitName;

    @ExcelProperty(value = "损耗率", index = 5)
    private String lossRate;

    @ExcelProperty(value = "数量*", index = 6)
    @NotBlank(message = "数量不能为空")
    private String quantity;

    @ExcelProperty(value = "主料/辅料", index = 7)
    private String typeName;

    @ExcelProperty(value = "单品重量/长度", index = 8)
    private String weightPerUnit;

    @ExcelProperty(value = "最低库存", index = 9)
    private String minStock;

    @ExcelProperty(value = "备注", index = 10)
    private String remark;
}
