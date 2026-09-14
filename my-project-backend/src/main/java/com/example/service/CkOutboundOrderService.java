package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.OutboundOrder;
import com.example.entity.cangku.req.OutboundListPageReq;

import java.util.Date;
import java.util.List;

// CkOutboundOrderService.java
public interface CkOutboundOrderService extends IService<OutboundOrder> {
    List<OutboundOrder> selectByProductIds(Long tenantId, List<Long> productIds, Integer status);

    List<OutboundOrder> selectByInboundOrderIds(Long tenantId, List<Long> outboundOrderIds);

    Page<OutboundOrder> getPage(Page<OutboundOrder> page, OutboundListPageReq req);

    OutboundOrder selectById(Long id, Long tenantId);

    /**
     * 审核通过状态 CAS：仅当当前 status=expectedStatus 时更新为 newStatus。
     * @return true 当且仅当影响行数 == 1
     */
    boolean casUpdateStatusForApprove(Long id, Long tenantId, Integer expectedStatus,
                                      Integer newStatus, Long userId, Date modifiedAt, String remark);

    List<OutboundOrder> selectByOutboundOrderIds(Long tenantId, List<Long> orderIds);

    List<OutboundOrder> selectByOutboundOrderNos(Long tenantId, List<String> outboundOrderNoList);

    List<OutboundOrder> selectCountsByInboundListPageReq(OutboundListPageReq req);

    Long selectCountsOfOutboundOrders(Long tenantId, Date date);

    Long selectCountsOfApprovals(Long tenantId);
}