package com.example.entity.cangku.req;

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
public class ProductImportDto {

    @ExcelProperty(value = "库号", index = 0)
    private String warehouseCode;

    @ExcelProperty(value = "名称", index = 1)
    @NotBlank(message = "产品名称不能为空")
    private String name;

    @ExcelProperty(value = "规格", index = 2)
    @NotBlank(message = "规格型号不能为空")
    private String spec;

    @ExcelProperty(value = "颜色", index = 3)
    private String color;

    @ExcelProperty(value = "入库单号及数量", index = 4)
    private String inStockInfo;

    @ExcelProperty(value = "出库单号及数量", index = 5)
    private String outStockInfo;

    @ExcelProperty(value = "客户INV/快递或者海运", index = 6)
    private String customerInfo;

    @ExcelProperty(value = "库存剩余", index = 7)
    private String stockRemaining; // 先用String接收，后续处理

    @ExcelProperty(value = "每箱数量", index = 8)
    private String quantityPerBox; // 先用String接收

    @ExcelProperty(value = "箱数", index = 9)
    private String boxCount; // 先用String接收

    @ExcelProperty(value = "长", index = 10)
    private String boxLength; // 先用String接收

    @ExcelProperty(value = "宽", index = 11)
    private String boxWidth; // 先用String接收

    @ExcelProperty(value = "高", index = 12)
    private String boxHeight; // 先用String接收

    @ExcelProperty(value = "体积(M3)", index = 13)
    private String volume; // 先用String接收

    @ExcelProperty(value = "单件重KG", index = 14)
    private String weightPerUnit; // 先用String接收

    @ExcelProperty(value = "总重量KG", index = 15)
    private String totalWeight; // 先用String接收

    @ExcelProperty(value = "备注", index = 16)
    private String remark;

    @ExcelProperty(value = "英文品名", index = 17)
    private String englishName;

    @ExcelProperty(value = "单价RMB", index = 18)
    private String unitPriceRmb; // 先用String接收

    @ExcelProperty(value = "合计RMB", index = 19)
    private String totalPriceRmb; // 先用String接收

    @ExcelProperty(value = "Price(USD)", index = 20)
    private String unitPriceUsd; // 先用String接收

    @ExcelProperty(value = "Total (USD)", index = 21)
    private String totalPriceUsd; // 先用String接收

    @ExcelProperty(value = "FYBQ250410出库存", index = 22)
    private String fybqStockOut;
}
