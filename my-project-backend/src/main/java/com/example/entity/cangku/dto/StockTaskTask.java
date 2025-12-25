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
 * @Date 2025/12/25 11:33
 */
//ck_stock_take_task
@Data
@TableName("ck_stock_take_task")
public class StockTaskTask extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //盘点单ID
    private Long stockTakeId;
    //任务编号
    private String taskNo;
    //任务名称
    private String taskName;
    //分配人ID
    private Long assignerId;
    //分配人姓名
    private String assignerName;
    //执行人ID
    private Long assigneeId;
    //执行人姓名
    private String assigneeName;
    //仓库ID
    private Long warehouseId;
    //仓库编码
    private String warehouseCode;
    //负责区域(JSON数组)
    private String areaCodes;
    //负责货架(JSON数组)
    private String shelfIds;
    //产品范围(JSON格式)
    private String productRange;
    //预计盘点项数
    private Integer estimatedItems;
    //实际盘点项数
    private Integer actualItems;
    //已完成项数
    private Integer completedItems;
    //准确项数
    private Integer accuracyItems;
    //差异项数
    private Integer differItems;
    //进度百分比
    private BigDecimal progressRate;
    //质量评分(0-10)
    private BigDecimal qualityScore;
    //效率评分
    private BigDecimal efficiencyScore;
    //状态:0-待开始,1-进行中,2-已完成,3-异常中断,4-已取消
    private Integer status;
    //计划开始时间
    private Date planStartTime;
    //计划结束时间
    private Date planEndTime;
    //实际开始时间
    private Date actualStartTime;
    //实际结束时间
    private Date actualEndTime;
    //耗时(分钟)
    private Integer durationMinutes;
    //使用设备类型
    private String deviceType;
    //设备ID
    private String deviceId;
    //任务备注
    private String remark;
    //中断原因
    private String abortReason;
}
