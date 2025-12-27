package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class StockListPageReq extends PageReq {
    //盘点单列表
    private String orderNo;
    //盘点类型
    private String takeType;
    //盘点策略
    private String takeStrategy;
    //仓库ID
    private Long warehouseId;
    //执行状态
    private Integer executeStatus;
    //审批状态
    private Integer approvalStatus;
    //时间范围
    private String[] dateRange;

    Long userId;
    Long tenantId;
}
