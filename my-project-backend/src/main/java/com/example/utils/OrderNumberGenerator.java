package com.example.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class OrderNumberGenerator {
    private static final AtomicLong counter = new AtomicLong(0);
    
    /**
     * 生成订单编号 (以ON开头)
     * 格式: ON + 年月日时分秒 + 时间戳 + 序列号
     */
    public static String generateOrderNo() {
        return generateNumber("ON");
    }

    public static String generateWareCode() {
        return generateNumber("WARE");
    }

    public static String generateShelfCode() {
        return generateNumber("SHELF");
    }

    /**
     * 订单批次码 (以PC开头)
     * 订单批次码格式: PC + 年月日时分秒 + 时间戳 + 随机数
     */
    public static String generateOrderPC() {
        return generateNumber("PC");
    }
    
    /**
     * 生成关联订单编号 (以RON开头)
     * 格式: RON + 年月日时分秒 + 时间戳 + 序列号
     */
    public static String generateRelatedOrderNo() {
        return generateNumber("RON");
    }
    
    private static String generateNumber(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        
        // 格式化年月日时分秒: yyyyMMddHHmmss
        String datetime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 时间戳 (毫秒)
        long timestamp = System.currentTimeMillis();
        
        // 序列号 (防止同一毫秒内重复)
        long sequence = counter.incrementAndGet() % 1000;
        
        return String.format("%s%s%d%03d", prefix, datetime, timestamp, sequence);
    }
    
    // 测试方法
    public static void main(String[] args) {
        System.out.println("Order No: " + generateOrderNo());
        System.out.println("Related Order No: " + generateRelatedOrderNo());
        
        // 测试生成多个
        for (int i = 0; i < 5; i++) {
            System.out.println("Order No " + (i+1) + ": " + generateOrderNo());
        }
    }
}