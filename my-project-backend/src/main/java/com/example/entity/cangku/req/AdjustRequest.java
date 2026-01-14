package com.example.entity.cangku.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdjustRequest {
    private Long id;
    private String adjustNo;//调整单号
    private Long warehouseId;//仓库ID
    private String warehouseName;//仓库名称
    private Integer adjustType; // 1-盘点调整、2-报损调整、3-报溢调整、4-成本调整、5-库存转移、6-其他调整
    private Integer sourceType; // 1-盘点单、2-手动创建、3-异常处理、4-系统自动
    private Long sourceId;//数据来源，如果是盘点调整，则是盘点单ID
    private String sourceNo;//数据来源单号 ，如果是盘点调整，则是盘点单号
    private String adjustReason;//调整原因
    private Boolean isUrgent;//是否紧急
    private Boolean isAffectCost;//是否影响成本
    private Integer priority; // 1-紧急、2-高、3-中、4-低
    private String expectExecuteTime;//预计执行时间
    private String remark;//备注
    private Long approvalUserId;//审批人ID
    private Long userId;//创建人ID
    private Long tenantId;//租户ID
    private List<AdjustItemRequest> items;

    @Data
    public static class AdjustItemRequest {
        private Long productId;//商品ID
        private String productName;//商品名称
        private String productCode;//商品编码
        private String skuCode;//商品SKU编码
        private String spec;//商品规格
        private String unit;// 单位
        private String batchNo;//批次号
        private Long shelfId;//货架ID
        private String shelfCode;//货架编码
        private String locationCode;//位置编码
        private BigDecimal beforeQuantity;//库存数量
        private BigDecimal adjustQuantity; // 正数表示增加，负数表示减少
        private BigDecimal afterQuantity;
        private BigDecimal unitCost;//单位成本
        private BigDecimal adjustCostAmount;//调整金额
        private BigDecimal unitPrice;//价格单位
        private BigDecimal adjustAmount;//调整金额
        private String itemReason;//调整原因
        private String itemRemark;//备注
        private Integer inventoryType; // 1-普通库存、2-批次库存、3-货架库存
        private Integer costAdjustMethod; // 1-加权平均、2-移动平均、3-指定批次
        private Long sourceItemId;//数据来源项ID
        private String extData;//扩展数据
    }
}