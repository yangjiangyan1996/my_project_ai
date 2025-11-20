package com.example.utils;

import cn.hutool.extra.pinyin.PinyinUtil;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/21 00:51
 */
public class ChineseUtils {
    /**
     * 将中文字符串转换为拼音
     * @param chineseText 中文字符串
     * @return 拼音字符串（带空格分隔）
     */
    public static String chineseToPinyin(String chineseText) {
        if (chineseText == null || chineseText.trim().isEmpty()) {
            return "";
        }

        if (chineseText == null || chineseText.trim().isEmpty()) {
            return "";
        }

        // 检查字符串是否全是字母（包括大小写）
        if (chineseText.matches("[a-zA-Z]+")) {
            return chineseText;
        }
        return PinyinUtil.getPinyin(chineseText, " ");
    }

    /**
     * 将中文字符串转换为拼音（不带空格）
     * @param chineseText 中文字符串
     * @return 拼音字符串（连续）
     */
    public static String chineseToPinyinWithoutSpace(String chineseText) {
        if (chineseText == null || chineseText.trim().isEmpty()) {
            return "";
        }
        return PinyinUtil.getPinyin(chineseText, "");
    }

    /**
     * 将中文字符串转换为拼音首字母
     * @param chineseText 中文字符串
     * @return 拼音首字母字符串
     */
    public static String chineseToPinyinInitials(String chineseText) {
        if (chineseText == null || chineseText.trim().isEmpty()) {
            return "";
        }
        return PinyinUtil.getFirstLetter(chineseText, "");
    }
}
