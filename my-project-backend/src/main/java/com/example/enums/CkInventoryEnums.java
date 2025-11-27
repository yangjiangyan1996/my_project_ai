package com.example.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 20:07
 */
public class CkInventoryEnums {

    @Getter
    public enum ChartsWarningLevel {
        ALL("all", "全部",9),
        URGENT("urgent", "紧急预警",0),
        WARNING("warning", "一般预警",1),
        NORMAL("normal", "库存正常",8);

        private String code;
        private String desc;
        private Integer sort;

        ChartsWarningLevel(String code, String desc, Integer sort) {
            this.code = code;
            this.desc = desc;
            this.sort = sort;
        }

        public static String getDescByCode(String code) {
            for (ChartsWarningLevel value : ChartsWarningLevel.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return "";
        }
    }


    @Getter
    public enum StockStatus {
        IN_STOCK(1, "有货"),
        OUT_OF_STOCK(0, "无货"),
        LOW_STOCK(10, "低库存");

        private final Integer code;
        private final String desc;

        StockStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 根据库存数量获取库存状态
         *
         * @param inventory 库存数量
         * @param minStock  最小库存数量
         * @return
         */
        public static Integer getCodeByCount(BigDecimal inventory, BigDecimal minStock) {
            // 参数校验
            if (inventory == null) {
                return OUT_OF_STOCK.getCode();
            }

            if (minStock == null) {
                minStock = BigDecimal.ZERO;
            }

            // 库存小于等于0，返回无货
            if (inventory.compareTo(BigDecimal.ZERO) <= 0) {
                return OUT_OF_STOCK.getCode();
            }

            // 库存大于0但小于等于最小库存，返回低库存
            if (inventory.compareTo(minStock) <= 0) {
                return LOW_STOCK.getCode();
            }

            // 库存大于最小库存，返回有货
            return IN_STOCK.getCode();
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
