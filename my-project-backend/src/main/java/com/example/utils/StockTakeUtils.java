package com.example.utils;

import com.example.enums.CkStockTakeEnums;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 盘点工具类
 */
public class StockTakeUtils {
    
    private StockTakeUtils() {
        // 工具类，私有构造
    }
    
    /**
     * 计算差异率
     * @param systemQuantity 系统数量
     * @param actualQuantity 实际数量
     * @return 差异率（百分比，保留4位小数）
     */
    public static BigDecimal calculateDifferenceRate(BigDecimal systemQuantity, BigDecimal actualQuantity) {
        if (systemQuantity == null || systemQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal difference = actualQuantity.subtract(systemQuantity);
        return difference.divide(systemQuantity, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * 检查是否需要复盘
     * @param differenceRate 差异率
     * @param toleranceRate 允许差异率
     * @return true:需要复盘, false:不需要复盘
     */
    public static boolean needSecondCount(BigDecimal differenceRate, BigDecimal toleranceRate) {
        if (differenceRate == null || toleranceRate == null) {
            return false;
        }
        
        BigDecimal absRate = differenceRate.abs();
        BigDecimal absTolerance = toleranceRate.abs();
        
        return absRate.compareTo(absTolerance) > 0;
    }
    
    /**
     * 生成盘点单号
     * @param tenantId 租户ID
     * @param warehouseCode 仓库编码
     * @return 盘点单号
     */
    public static String generateTakeOrderNo(Long tenantId, String warehouseCode) {
        String prefix = "PD";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        String random = String.valueOf((int)((Math.random() * 9 + 1) * 1000));
        
        return String.format("%s%s%s%s%s", 
                prefix, 
                String.format("%04d", tenantId % 10000),
                warehouseCode,
                timestamp,
                random);
    }
    
    /**
     * 生成盘点任务编号
     * @param takeOrderNo 盘点单号
     * @param taskSequence 任务序号
     * @return 任务编号
     */
    public static String generateTaskNo(String takeOrderNo, Integer taskSequence) {
        return String.format("%s-T%03d", takeOrderNo, taskSequence);
    }
    
    /**
     * 验证盘点数据是否有效
     * @param actualQuantity 实际数量
     * @param firstCountQuantity 初盘数量
     * @param secondCountQuantity 复盘数量
     * @return 错误信息，null表示有效
     */
    public static String validateCountData(BigDecimal actualQuantity, 
                                          BigDecimal firstCountQuantity, 
                                          BigDecimal secondCountQuantity) {
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return "实际数量不能为空且必须大于等于0";
        }
        
        if (firstCountQuantity == null && secondCountQuantity == null) {
            return "至少需要有一次盘点数据";
        }
        
        if (firstCountQuantity != null && firstCountQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return "初盘数量不能小于0";
        }
        
        if (secondCountQuantity != null && secondCountQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return "复盘数量不能小于0";
        }
        
        return null;
    }
    
    /**
     * 计算盘点进度
     * @param totalItems 总项数
     * @param completedItems 已完成项数
     * @return 进度百分比
     */
    public static BigDecimal calculateProgressRate(Integer totalItems, Integer completedItems) {
        if (totalItems == null || totalItems == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = new BigDecimal(completedItems)
                .divide(new BigDecimal(totalItems), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return rate.setScale(2, RoundingMode.HALF_UP);
    }
}