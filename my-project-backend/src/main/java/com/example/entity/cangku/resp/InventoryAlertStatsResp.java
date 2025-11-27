package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 21:02
 */
@Data
public class InventoryAlertStatsResp {
    private Integer totalProducts;     // 监控产品总数
    private Long urgentAlerts;      // 紧急预警数量
    private Long warningAlerts;     // 一般预警数量
    private Long normalProducts;    // 库存正常数量
}
