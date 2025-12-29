package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:42
 */
@Data
public class StockItemListPageResp {
    /** 盘点单ID */
    private Long stockTakeId;

    /** 租户ID */
    private Long tenantId;

    /** 盘点单详情ID */
    private Long stockTakeItemId;

    /** 产品ID */
    private Long productId;
    //商品名称
    private String productName;
    private String sku;
    //规格型号
    private String spec;
    //颜色
    private String color;

    /** 批次号 */
    private String batchNo;

    /** 货架ID */
    private Long shelfId;

    private String shelfName;

    /** 系统库存数量 */
    private BigDecimal systemQuantity;

    /** 实盘数量 */
    private BigDecimal countedQuantity;

    /** 差异数量 */
    private BigDecimal diffQuantity;

    /** 状态:1-未盘,2-已盘,3-已确认 */
    private Integer status;

    /** 是否有盘点任务权限 */
    private Boolean hasStockItemTaskPermission;
}
