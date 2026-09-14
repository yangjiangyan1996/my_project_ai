package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InboundOrder;
import com.example.entity.cangku.req.InboundListPageReq;

import java.util.Date;
import java.util.List;

// CkInboundOrderService.java
public interface CkInboundOrderService extends IService<InboundOrder> {
    Page<InboundOrder> getPage(Page<InboundOrder> page, InboundListPageReq req);

    InboundOrder selectById(Long orderId, Long tenantId);

    /**
     * 审核通过状态 CAS：仅当当前 status=expectedStatus 时更新为 newStatus。
     * @return true 当且仅当影响行数 == 1
     */
    boolean casUpdateStatusForApprove(Long id, Long tenantId, Integer expectedStatus,
                                      Integer newStatus, Long userId, Date modifiedAt);

    List<InboundOrder> selectByInboundOrderIds(Long tenantId, List<Long> inboundOrderIds);

    List<InboundOrder> selectCountsByInboundListPageReq(InboundListPageReq tenantId);

    Long selectCountsOfInboundOrders(Long tenantId, Date date);

    Long selectCountsOfApprovals(Long tenantId);
}