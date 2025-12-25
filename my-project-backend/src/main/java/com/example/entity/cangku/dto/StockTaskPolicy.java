package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:17
 */
@Data
@TableName("ck_stock_take_policy")
public class StockTaskPolicy  extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //策略编码
    private String policyCode;
    //策略名称
    private String policyName;
    //策略描述
    private String policyDesc;
    //适用仓库(NULL表示全部)
    private Long warehouseId;
    //适用仓库编码列表
    private String warehouseCodes;
    //触发类型:1-定期盘点,2-库存阈值触发,3-业务触发,4-随机抽盘,5-异常触发
    private Integer triggerType;
    //周期天数
    private Integer cycleDays;
    //周期星期(如:1,3,5表示周一三五)
    private String cycleWeekday;
    //执行时间
    private String cycleTime;
    //最小库存价值触发
    private BigDecimal minInventoryValue;
    //最大未盘天数
    private Integer maxDaysNoTake;
    //库存周转率阈值
    private BigDecimal inventoryTurnoverThreshold;
    //包含分类(JSON数组)
    private String includeCategories;
    //排除分类(JSON数组)
    private String excludeCategories;
    //包含产品(JSON数组)
    private String includeProducts;
    //排除产品(JSON数组)
    private String excludeProducts;
    //最低单价
    private BigDecimal minUnitPrice;
    //最高单价
    private BigDecimal maxUnitPrice;
    //ABC分类:A,B,C,ALL
    private String abcClass;
    //盘点方式:1-动态,2-静态
    private Integer takeType;
    //盘点策略:1-全库,2-区域,3-分类,4-循环
    private Integer takeStrategy;
    //每日最大盘点项
    private Integer maxItemsPerDay;
    //最大持续小时
    private Integer maxDurationHours;
    //是否需要审批
    private Boolean requireApproval;
    //审批流程ID
    private Long approvalFlowId;
    //允许差异率%
    private BigDecimal toleranceRate;
    //是否需要复盘
    private Boolean requireSecondCount;
    //复盘阈值%
    private BigDecimal secondCountThreshold;
    //是否自动生成
    private Boolean autoGenerate;
    //是否自动开始
    private Boolean autoStart;
    //是否自动关闭
    private Boolean autoClose;
    //下次执行时间
    private Date nextExecuteTime;
    //上次执行时间
    private Date lastExecuteTime;
    //执行次数
    private Integer executeCount;
    //状态:0-停用,1-启用
    private Integer status;
}
