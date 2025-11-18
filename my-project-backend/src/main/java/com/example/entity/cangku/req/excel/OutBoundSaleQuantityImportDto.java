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
public class OutBoundSaleQuantityImportDto {
    @ExcelProperty(value = "id", index = 0)
    @NotBlank(message = "产品id不能为空")
    private String productId;

    @ExcelProperty(value = "sku", index = 1)
    @NotBlank(message = "SKU不能为空")
    private String sku;

    @ExcelProperty(value = "名称", index = 2)
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @ExcelProperty(value = "规格", index = 3)
    @NotBlank(message = "规格不能为空")
    private String spec;

    @ExcelProperty(value = "颜色", index = 4)
    @NotBlank(message = "颜色不能为空")
    private String color;

    @ExcelProperty(value = "库存", index = 5)
    @NotBlank(message = "库存不能为空")
    private String inventory;


    @ExcelProperty(value = "数量", index = 6)
    private String quantity;

    @ExcelProperty(value = "价格(人民币)", index = 7)
    private String price;

    @ExcelProperty(value = "价格(美元)", index = 8)
    private String priceUsd;

    @ExcelProperty(value = "备注", index = 9)
    private String remark;


}
