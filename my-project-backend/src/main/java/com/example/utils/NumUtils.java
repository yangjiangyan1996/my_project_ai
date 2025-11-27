package com.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/21 01:04
 */
public class NumUtils {
    /**
     * 把百分数转换成小数
     * @param percentStr 百分比字符串
     * @return 转换后的小数，如果不是有效的百分比格式返回null
     */
    public static BigDecimal parsePercentStrict(String percentStr) {
        if (percentStr == null || percentStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = percentStr.trim();

        // 使用正则表达式验证格式：数字（可含小数点）后跟%符号
        if (trimmed.matches("^-?\\d+(\\.\\d+)?%$")) {
            try {
                String numberPart = trimmed.substring(0, trimmed.length() - 1);
                double value = Double.parseDouble(numberPart);
                // 使用BigDecimal进行精确计算并保留4位小数
                BigDecimal decimal = new BigDecimal(String.valueOf(value));
                BigDecimal result = decimal.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                return result;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }


    /**
     * 将小数转换为百分比字符串
     * @param decimal 小数，例如0.1234
     * @param scale 保留小数位数，默认2位
     * @return 百分比字符串，例如"12.34%"
     */
    public static String toPercentString(BigDecimal decimal, int scale) {
        if (decimal == null) {
            return null;
        }

        // 乘以100并设置精度
        BigDecimal percent = decimal.multiply(new BigDecimal("100"));
        percent = percent.setScale(scale, RoundingMode.HALF_UP);

        return percent.stripTrailingZeros().toPlainString() + "%";
    }
}
