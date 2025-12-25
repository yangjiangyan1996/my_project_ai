package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:20
 */
//ck_stock_take_summary
@Data
@TableName("ck_stock_take_summary")
public class StockTaskSummary extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //汇总日期
    private java.util.Date summaryDate;
    //时间维度:1-日,2-周,3-月,4-季度,5-年
    private Integer timeDimension;
    //仓库ID
    private Long warehouseId;
    //仓库编码
    private String warehouseCode;
    //盘点次数
    private Integer takeCount;
    //总盘点项数
    private Integer totalItems;
    //已完成项数
    private Integer completedItems;
    //准确项数
    private Integer accuracyItems;
    //差异项数
    private Integer differItems;
    //准确率%
    private BigDecimal accuracyRate;
    //差异率%
    private BigDecimal differRate;
    //系统总价值
    private BigDecimal totalSystemValue;
    //实际总价值
    private BigDecimal totalActualValue;
    //差异总价值
    private BigDecimal totalDifferenceValue;
    //平均差异数量
    private BigDecimal avgDifferenceQuantity;
    //最大差异数量
    private BigDecimal maxDifferenceQuantity;
    //最常见原因1
    private Integer commonReason1;
    //原因1次数
    private Integer commonReason1Count;
    //最常见原因2
    private Integer commonReason2;
    //原因2次数
    private Integer commonReason2Count;
    //最常见原因3
    private Integer commonReason3;
    //原因3次数
    private Integer commonReason3Count;
    //高频差异产品(JSON)
    private String highDifferProducts;
    //高频差异区域(JSON)
    private String highDifferAreas;
}
