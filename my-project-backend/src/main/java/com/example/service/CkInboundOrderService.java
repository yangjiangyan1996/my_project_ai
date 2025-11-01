package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.InboundOrder;
import com.example.entity.cangku.req.InboundListPageReq;

// CkInboundOrderService.java
public interface CkInboundOrderService extends IService<InboundOrder> {
    Page<InboundOrder> getPage(Page<InboundOrder> page, InboundListPageReq req);
}