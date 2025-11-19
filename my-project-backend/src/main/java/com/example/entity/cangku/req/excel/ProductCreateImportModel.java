package com.example.entity.cangku.req.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/19 12:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateImportModel {
    //英文名称
    @ExcelProperty(value = "英文品名", index = 0)
    @NotBlank(message = "英文品名不能为空")
    private String englishName;

    //sku编号
    @ExcelProperty(value = "sku", index = 1)
    @NotBlank(message = "sku不能为空")
    private String sku;
    //商品名称
    @ExcelProperty(value = "名称", index = 2)
    @NotBlank(message = "名称不能为空")
    private String name;

    //规格型号
    @ExcelProperty(value = "规格", index = 3)
    @NotBlank(message = "规格不能为空")
    private String spec;
    //颜色
    @ExcelProperty(value = "颜色", index = 4)
    @NotBlank(message = "颜色不能为空")
    private String color;
    //出货单位数量，比如出货时一箱子多少货
    @ExcelProperty(value = "每箱数量", index = 5)
    private String outUnitPerNum;

    //出货单位的体积：长度
    @ExcelProperty(value = "出货单位体积长cm", index = 6)
    private  String outUnitLength;


    //出货单位的体积：宽度
    @ExcelProperty(value = "出货单位体积宽度cm", index = 7)
    private  String outUnitWidth;

    //出货单位的体积：高度
    @ExcelProperty(value = "出货单位体积高度cm", index = 8)
    private  String outUnitHeight;


    //单品重量(可选)
    @ExcelProperty(value = "单件重KG", index = 9)
    @NotBlank(message = "单品重量不能为空")
    private String weightPerUnit;

    //最低库存
    @ExcelProperty(value = "最低库存", index = 10)
    @NotBlank(message = "最低库存不能为空")
    private String minStock;

    // 备注
    @ExcelProperty(value = "备注", index = 11)
    private String remark;

}
