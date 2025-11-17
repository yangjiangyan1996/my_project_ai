package com.example.entity.cangku.resp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/17 23:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboundSaleExcelModel {
    @ExcelProperty("id")
    private String productId;

    @ExcelProperty("sku")
    private String sku;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("颜色")
    private String color;

    @ExcelProperty("库存")
    private String inventory;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("价格")
    private String price;

    @ExcelProperty("备注")
    private String remark;
}
