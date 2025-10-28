package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.time.LocalDateTime;

// 库存盘点单主表
@Data
@TableName("ck_stock_take")
public class StockTake extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //盘点单号
    private String orderNo;
    //仓库ID
    private Long warehouseId;
    //状态:0-盘点中,1-已完成,9-已取消
    private Integer status;
    //盘点开始时间
    private LocalDateTime startTime;
    //盘点结束时间
    private LocalDateTime endTime;
    //总差异数量
    private Integer totalDifference;
    //备注
    private String remark;
}