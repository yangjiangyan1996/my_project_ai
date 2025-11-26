package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 00:14
 */
public class CkProductEnums {

    //`type` tinyint DEFAULT '2' COMMENT '1=主料，2=辅料',

    /**
     *  bom详情类型
     */
    @Getter
    public enum BomDetailType {
        BOM_DETAIL_TYPE_MAIN_MATERIAL(1, "主料"),
        BOM_DETAIL_TYPE_ACCESSORY(2, "辅料");

        private Integer code;
        private String desc;
        BomDetailType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
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
