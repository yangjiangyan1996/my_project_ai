package com.example.entity.cangku.req.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 产品导入DTO - EasyExcel版本
 */
@ColumnWidth(15)
/**
 * 产品导入DTO
 */
@Data
public class SkuImportDto {
    @ExcelProperty(value = "名称", index = 0)
    @NotBlank(message = "产品名称不能为空")
    private String name;

    @ExcelProperty(value = "规格", index = 1)
    @NotBlank(message = "规格型号不能为空")
    private String spec;

    @ExcelProperty(value = "颜色", index = 2)
    private String color;

    @ExcelProperty(value = "用户的sku", index = 3)
    private String productSku;

    @ExcelProperty(value = "客户sku", index = 4)
    private String customerSku;

}
