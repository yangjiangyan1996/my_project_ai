package com.example.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/14 11:11
 */
public class DateUtils {
    /**
     * 生成当前时间的Long类型时间戳，格式为yyyyMMddHHmmss
     * @return 例如20250714094544这样的Long数字
     */
    public static long generateTimestamp() {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();

        // 定义格式化为yyyyMMddHHmmss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // 格式化并转换为Long
        return Long.parseLong(now.format(formatter));
    }

    //Date转换字符串的方法
    public static String date2Str(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    //字符串转换Date的方法
    public static Date str2Date(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
