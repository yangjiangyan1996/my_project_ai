package com.example.utils;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/12 01:23
 */
public class MoneyUtils {
    public static BigDecimal cleanCurrencyString(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return new BigDecimal(0);
        }
        String trim = currencyStr.replaceAll("[¥$€£\\s]", "").trim();
        return new BigDecimal(trim);
    }
}
