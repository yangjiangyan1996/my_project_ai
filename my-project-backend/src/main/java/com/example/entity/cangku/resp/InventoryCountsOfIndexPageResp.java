package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 21:02
 */
@Data
public class InventoryCountsOfIndexPageResp {
    //产品总数
    private Long totalProducts;
    //仓库数量
    private Long totalWarehouses;
    //今日入库
    private Long todayInbound;
    //今日出库
    private Long todayOutbound;
    //入库待办审批
    private Long todoInBoundApproval;
    //出库待办审批
    private Long todoOutBoundApproval;
    //最低库存
    private Long lowStock;
}
