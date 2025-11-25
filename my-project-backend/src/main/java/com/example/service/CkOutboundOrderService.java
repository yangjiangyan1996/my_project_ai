package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.OutboundOrder;
import com.example.entity.cangku.req.OutboundListPageReq;

import java.util.List;

// CkOutboundOrderService.java
public interface CkOutboundOrderService extends IService<OutboundOrder> {
    List<OutboundOrder> selectByProductIds(Long tenantId, List<Long> productIds, Integer status);

    List<OutboundOrder> selectByInboundOrderIds(Long tenantId, List<Long> outboundOrderIds);

    Page<OutboundOrder> getPage(Page<OutboundOrder> page, OutboundListPageReq req);

    OutboundOrder selectById(Long id, Long tenantId);

    List<OutboundOrder> selectByOutboundOrderIds(Long tenantId, List<Long> orderIds);

    List<OutboundOrder> selectByOutboundOrderNos(Long tenantId, List<String> outboundOrderNoList);

}