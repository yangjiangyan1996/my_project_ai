package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:42
 */
@Data
public class StockListPageResp {
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 盘点单号 */
    private String stockTakeNo;

    /** 仓库ID */
    private Long warehouseId;
    private String warehouseName;


    /** 盘点类型:1-动态盘,2-静态盘 */
    private Integer takeType;

    /** 盘点范围:1-仓库,2-区域,3-货架,4-库位,5-SKU */
    private Integer takeScope;

    //审批状态:1新建,2待审核,3通过,4拒绝
    private Integer approvalStatus;


    //盘点状态:0未开始,1盘点中,2待确认,3已完成,4已取消
    private Integer takeStatus;

    /** 盘点明细数量 */
    private Integer itemCount;

    /** 实盘数量 */
    private Integer countedCount;

    //差异数量
    private Integer totalDiff;

    /** 备注 */
    private String remark;

    private String createdByName;
    private Date createdAt;
    private Date modifiedAt;
}
