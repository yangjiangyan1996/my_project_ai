package com.example.utils;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/21 01:04
 */
public class NumUtils {
    /**
     * 严格验证的百分比转换方法
     * @param percentStr 百分比字符串
     * @return 转换后的小数，如果不是有效的百分比格式返回null
     */
    public static Double parsePercentStrict(String percentStr) {
        if (percentStr == null || percentStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = percentStr.trim();

        // 使用正则表达式验证格式：数字（可含小数点）后跟%符号
        if (trimmed.matches("^-?\\d+(\\.\\d+)?%$")) {
            try {
                String numberPart = trimmed.substring(0, trimmed.length() - 1);
                double value = Double.parseDouble(numberPart);
                return value / 100.0;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
