package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/7 14:52
 */
public class QuanEnum {

    @Getter
    public enum BarStatusEnums {
        WAIT_AUDIT(0, "待审核"),
        AUDIT_REJECT(10, "审核拒绝"),
        DISABLED(20, "已停用"),
        AUDIT_PASS(30, "审核通过");
        ;
        private Integer code;
        private String name;

        BarStatusEnums(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer v) {
            for (BarStatusEnums value : BarStatusEnums.values()) {
                if (value.code.equals(v)) {
                    return value.name;
                }
            }
            return null;
        }
    }

}
