package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
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
public class ProductCreateExportModel {
    //英文名称
    @ExcelProperty("英文品名")
    private String englishName;
    //sku编号
    @ExcelProperty("sku")
    private String sku;
    //商品名称
    @ExcelProperty("名称")
    private String name;

    //规格型号
    @ExcelProperty("规格")
    private String spec;
    //颜色
    @ExcelProperty("颜色")
    private String color;
    //出货单位数量，比如出货时一箱子多少货
    @ExcelProperty("每箱数量")
    private String outUnitPerNum;

    //出货单位的体积：长度
    @ExcelProperty("出货单位体积长cm")
    private  String outUnitLength;

    //出货单位的体积：宽度
    @ExcelProperty("出货单位体积宽度cm")
    private  String outUnitWidth;

    //出货单位的体积：高度
    @ExcelProperty("出货单位体积高度cm")
    private  String outUnitHeight;

    //单品重量(可选)
    @ExcelProperty("单件重KG")
    private String weightPerUnit;

    //最低库存
    @ExcelProperty("最低库存")
    private String minStock;

    // 备注
    @ExcelProperty("备注")
    private String remark;

}
