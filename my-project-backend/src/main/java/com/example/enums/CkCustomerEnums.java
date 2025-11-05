package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:09
 */
public class CkCustomerEnums {

    /**
     * 客户等级
     */
    @Getter
    public enum CustomerLevel {
        NORMAL(1, "普通客户"),
        VIP(2, "VIP客户"),
        STRATEGIC(3, "战略客户");
        private Integer code;
        private String desc;
        CustomerLevel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CustomerLevel value : CustomerLevel.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    /**
     * 客户类型
     */
    @Getter
    public enum CustomerType {
        ENTERPRISE( 1,"企业客户"),
        PERSONAL(2, "个人客户"),
        AGENT(3, "代理商"),
        DEALER(4, "经销商");
        private Integer  code;
        private String desc;
        CustomerType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CustomerType value : CustomerType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
}
