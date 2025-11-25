package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/25 12:26
 */
@Data
public class InboundProductInDetailResp {// 基本信息字段
    private Long id;
    private String orderNo;
    private Integer orderType;
    private String relatedOrderNo;  // 新增：关联领料单号（用于显示在基本信息区域）
    private String remark;
    private Integer status;
    private Long warehouseId;
    private String warehouseName;
    private String expectedDate;     // 新增：预计入库日期（字符串格式 YYYY-MM-DD）
    private java.util.Date createdAt;
    private java.util.Date modifiedAt;

    // 统计信息字段
    private Integer itemCount;       // 产品种类数量
    private BigDecimal totalQuantity; // 入库总数

    // 产品明细列表 - 这是最重要的部分
    private List<InboundItemDetail> items;

    @Data
    public static class InboundItemDetail {
        // 产品基本信息
        private Long productId;
        private String productName;
        private String sku;
        private String spec;
        private String unit;

        // 入库信息
        private BigDecimal actualQuantity;
        private String batchNo;
        private String remark;

        // 关联领料单信息 - 新增：用于显示"关联领料单"列
        private String relatedPickingOrderNo;  // 新增：关联的领料单号
        private Long relatedPickingOrderId;    // 新增：关联的领料单ID
        //生产任务ID
        private Long productionTaskId;


        // 货架信息
        private List<Long> shelfLocationIds;   // 新增：货架位置ID数组（多选）
        private List<ShelfAllocationDetail> shelfAllocations; // 货架分配明细

        // 原始数据ID（用于编辑时识别）
        private Long itemId;
    }

    @Data
    public static class ShelfAllocationDetail {
        private Long shelfLocationId;
        private String shelfLocationName;
        private String shelfCode;              // 新增：货架编码
        private String shelfName;              // 新增：货架名称
        private BigDecimal quantity;
    }

    // 系统字段
    private Long tenantId;
    private Long userId;
}
