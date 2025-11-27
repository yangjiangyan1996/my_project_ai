package com.example.constants;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 10:36
 */
public class CkCommonConstant {
    /**
     * 默认的数量
     */
    //最小库存数量
    public static final Long NUM_MIN_STOCK_QUANTITY = 100L;




    /**
     * 前缀
     */
    //barCode
    public static final String PREFIX_BAR_CODE = "BC_";
    //sku
    public static final String PREFIX_SKU = "SKU_";
    //bom
    public static final String PREFIX_BOM = "BOM_";





    /**
     * 初始化数据使用
     */
    //系统自动生成
    public static final String AUTO_GENERATE = "系统自动生成";
    //通用成品分类
    public static final String COMMON_CATEGORY_CP_NAME = "通用成品分类";
    //通用成品分类code
    public static final String COMMON_CATEGORY_CP_CODE = "common-category-cp";
    //通用原料分类名称
    public static final String COMMON_CATEGORY_YL_NAME = "通用原料分类";
    //通用原料分类code
    public static final String COMMON_CATEGORY_YL_CODE = "common-category-yl";

    //系统默认生产的单个单位
    public static final String DEFAULT_UNIT_NAME_GE = "个";
    //系统默认生产的单个单位code
    public static final String DEFAULT_UNIT_CODE_GE = "ge";

    //系统默认生产的批次单位
    public static final String DEFAULT_UNIT_NAME_XIANG = "箱";
    //系统默认生产的批次单位code
    public static final String DEFAULT_UNIT_CODE_XIANG = "xiang";
}
