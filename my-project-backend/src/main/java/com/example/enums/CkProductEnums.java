package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 00:14
 */
public class CkProductEnums {
    //  图片类型
    @Getter
    public enum ProductImageType {
        MAIN_IMAGE(1, "主图"),
        SUPPLEMENTARY_IMAGE(2, "补充图片"),
        ;
        private Integer code;
        private String desc;
        ProductImageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public static String getDescByCode(Integer code) {
            for (CkProductEnums.ProductImageType value : CkProductEnums.ProductImageType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

    //`type` tinyint DEFAULT '2' COMMENT '0=空标签 1=主料， 2=布料，10=辅料,  20=五金 999=包装，

    /**
     *  bom详情类型
     */
    @Getter
    public enum BomDetailType {

        BOM_DETAIL_TYPE_EMPTY(0, "空"),
        BOM_DETAIL_TYPE_MAIN_MATERIAL(1, "主"),
        BOM_DETAIL_TYPE_FABRIC(2, "布"),
        BOM_DETAIL_TYPE_ACCESSORY(10, "辅"),
        BOM_DETAIL_TYPE_HARDWARE(20, "五金"),
        BOM_DETAIL_TYPE_PACKAGE(100, "包装"),
        ;

        private Integer code;
        private String desc;
        BomDetailType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static Integer getNameLike(String name) {
            if (name == null) {
                return BOM_DETAIL_TYPE_EMPTY.getCode();
            }
            for (CkProductEnums.BomDetailType value : CkProductEnums.BomDetailType.values()) {
                if (name.contains(value.getDesc())) {
                    return value.getCode();
                }
            }
            return BOM_DETAIL_TYPE_EMPTY.getCode();
        }

        public static String getDescByCode(Integer code) {
            for (CkProductEnums.BomDetailType value : CkProductEnums.BomDetailType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

}
