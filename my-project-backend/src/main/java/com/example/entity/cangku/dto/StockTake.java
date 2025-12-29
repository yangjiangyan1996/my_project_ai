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
 * @Date 2025/12/25 11:06
 */
//ck_stock_take
@Data
@TableName("ck_stock_take")
public class StockTake extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 盘点单号 */
    private String stockTakeNo;

    /** 仓库ID */
    private Long warehouseId;

    /** 盘点类型:1-动态盘,2-静态盘 */
    private Integer takeType;

    /**
    /**
     * 锁定范围:1: '全部', 2: '批次',  3: '货架', 4: '商品'
     * {@link com.example.enums.CkStockTakeEnums.StockTakeScope}
     */
    private Integer takeScope;
    //盘点范围的业务值， 盘点范围:1: 全部 (仓库 )--无须存储， 2: 批次 -批次号 3: 货架 -货架ID 4: 指定商品-商品ID
    private String taskScopeValue;

    //审批状态:1新建,2待审核,3通过,4拒绝
    private Integer approvalStatus;

    //盘点状态:0未开始,1盘点中,2待确认,3已完成,4已取消
    private Integer takeStatus;

    /** 备注 */
    private String remark;

    //快照时间
    private Date snapshotTime;
    //关联的库存调整单ID
    private Long adjustOrderId;
    //总盘点项数
    private Integer totalItems;
    //已盘点项数
    private Integer countedItems;
    //差异项数
    private Integer diffItems;
    //版本号(乐观锁)
    private Integer version;
    //计划开始时间
    private Date planStartTime;
    //计划结束时间
    private Date planEndTime;
    //实际开始时间
    private Date actualStartTime;
    //实际结束时间
    private Date actualEndTime;
}
