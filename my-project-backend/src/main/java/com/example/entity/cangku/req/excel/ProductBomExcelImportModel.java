package com.example.entity.cangku.req.excel;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty(value = "主料/辅料", index = 1)
    private String typeName;

    @ExcelProperty(value = "名称*", index = 2)
    private String name;

    @ExcelProperty(value = "备注", index = 3)
    private String remark;

    //规格型号
    @ExcelProperty(value = "规格", index = 4)
    private String spec;


    //颜色
    @ExcelProperty(value = "颜色", index = 5)
    private String color;

    @ExcelProperty(value = "数量", index = 6)
    private String otherQuantity;



    //其他数量（比如有的东西是几米的，一次用几个，目前没有逻辑相关）

    //生产一个成品所需的原料净用量数量（目前只存储，无逻辑）
    @ExcelProperty(value = "净用量", index = 7)
    private String quantityBeforeLoss;

    @ExcelProperty(value = "单位名称", index = 8)
    private String unitName;


    @ExcelProperty(value = "损耗率(%)", index = 9)
    private String lossRate;

    @ExcelProperty(value = "总用量", index = 10)
    private String quantity;

    @ExcelProperty(value = "单品重量/长度", index = 11)
    private String weightPerUnit;

    @ExcelProperty(value = "最低库存", index = 12)
    private String minStock;

}
