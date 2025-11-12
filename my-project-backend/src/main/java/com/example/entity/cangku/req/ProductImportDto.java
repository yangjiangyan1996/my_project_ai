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

    @ExcelProperty(value = "货架号", index = 0)
    private String shelfName;

    @ExcelProperty(value = "名称", index = 1)
    @NotBlank(message = "产品名称不能为空")
    private String name;

    @ExcelProperty(value = "规格", index = 2)
    @NotBlank(message = "规格型号不能为空")
    private String spec;

    @ExcelProperty(value = "颜色", index = 3)
    private String color;

    @ExcelProperty(value = "入库库名及数量", index = 4)
    private String inStockInfo1;

    @ExcelProperty(value = "入库库名及数量", index = 5)
    private String inStockInfo2;

    @ExcelProperty(value = "入库库名及数量", index = 6)
    private String inStockInfo3;

    @ExcelProperty(value = "入库库名及数量", index = 7)
    private String inStockInfo4;


    @ExcelProperty(value = "库存剩余 (计算)", index = 8)
    private String stockRemaining; // 先用String接收，后续处理


    @ExcelProperty(value = "出库库名及数量", index = 9)
    private String outStockInfo1;

    @ExcelProperty(value = "出库库名及数量", index = 10)
    private String outStockInfo2;

    @ExcelProperty(value = "出库库名及数量", index = 11)
    private String outStockInfo3;

    @ExcelProperty(value = "出库库名及数量", index = 12)
    private String outStockInfo4;

    @ExcelProperty(value = "出库库名及数量", index = 13)
    private String outStockInfo5;

    @ExcelProperty(value = "出库库名及数量", index = 14)
    private String outStockInfo6;

    @ExcelProperty(value = "出库库名及数量", index = 15)
    private String outStockInfo7;

    @ExcelProperty(value = "出库数量", index = 16)
    private String outBoundCount;

    @ExcelProperty(value = "每箱数量", index = 17)
    private String quantityPerBox; // 先用String接收

    @ExcelProperty(value = "箱数(计算)", index = 18)
    private String boxCount; // 先用String接收

    @ExcelProperty(value = "长", index = 19)
    private String boxLength; // 先用String接收

    @ExcelProperty(value = "宽", index = 20)
    private String boxWidth; // 先用String接收

    @ExcelProperty(value = "高", index = 21)
    private String boxHeight; // 先用String接收

    @ExcelProperty(value = "体积(M3)(计算)", index = 22)
    private String volume; // 先用String接收

    @ExcelProperty(value = "单件重KG", index = 23)
    private String weightPerUnit; // 先用String接收

    @ExcelProperty(value = "总重量KG(计算)", index = 24)
    private String totalWeight; // 先用String接收

    @ExcelProperty(value = "备注", index = 25)
    private String remark;

    @ExcelProperty(value = "条形码", index = 26)
    private String barCode;

    @ExcelProperty(value = "sku", index = 27)
    private String sku;

    @ExcelProperty(value = "单价RMB", index = 28)
    private String unitPriceRmb; // 先用String接收

    @ExcelProperty(value = "合计RMB(计算)", index = 29)
    private String totalPriceRmb; // 先用String接收

    @ExcelProperty(value = "Price(USD)", index = 30)
    private String unitPriceUsd; // 先用String接收

    @ExcelProperty(value = "Total (USD)(计算)", index = 31)
    private String totalPriceUsd; // 先用String接收

}
