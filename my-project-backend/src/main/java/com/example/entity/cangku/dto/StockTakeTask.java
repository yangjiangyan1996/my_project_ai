package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 12:50
 */
//ck_stock_take_task
@Data
@TableName("ck_stock_take_task")
public class StockTakeTask  extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;
    //盘点单ID
    private Long stockTakeId;
    //    任务编号
    private String taskNo;
    //任务名称
    private String taskName;
    //盘点单号
    private String stockTakeNo;
    //仓库ID
    private Long warehouseId;

    /**
     * 分配类型
     * 1: 按人分配
     * 2: 按条件分配
     */
    private Integer assignType;

    /**
     * 分配维度（当assignType=2时使用）
     * 1: 按批次分配
     * 2: 按货架分配
     * 3: 按商品分配
     */
    private Integer assignDimension;
    //盘点类型:1-动态盘,2-静态盘
    private Integer stockTakeType;
    //负责范围(JSON数组)
    private String locationRange;
    //分配人ID
    private Long assigneeId;
    //执行人ID
    private Long executorId;
    //任务状态:1-待分配,2-已分配,3-执行中,4-已完成,5-已取消
    private Integer taskStatus;
    //优先级:1-紧急,2-高,3-中,4-低
    private Integer priority;
    //总项数
    private Integer totalItems;
    //已完成项数
    private Integer completedItems;
    //计划开始时间
    private Date planStartTime;
    //计划结束时间
    private Date planEndTime;
    //实际开始时间
    private Date actualStartTime;
    //实际结束时间
    private Date actualEndTime;
    //备注
    private String remark;
}
