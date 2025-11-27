package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 21:02
 */
@Data
public class InventoryAlertResp {
    private Long productId;
    private String productName;
    private String sku;
    //规格
    private String spec;
    //颜色
    private String color;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal currentStock;
    private BigDecimal warningThreshold;  // 预警库存线
    private BigDecimal urgentThreshold;   // 紧急库存线
    private String alertLevel;         // urgent-紧急, warning-预警, normal-正常
    private Integer alertLevelSort;         // 0-紧急, 1-预警, 2-正常
    private String lastUpdateTime;
}
