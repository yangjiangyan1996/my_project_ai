package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InboundOrder;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.mapper.CkInboundOrderMapper;
import com.example.service.CkInboundOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

// CkInboundOrderServiceImpl.java
@Service
public class CkInboundOrderServiceImpl extends ServiceImpl<CkInboundOrderMapper, InboundOrder> implements CkInboundOrderService {

    @Override
    public Page<InboundOrder> getPage(Page<InboundOrder> page, InboundListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<InboundOrder>()
                        .eq(req.getStatus() != null, "status", req.getStatus())
                        .ge(StringUtils.isNotBlank(req.getStartDate()), "created_at", req.getStartDate())
                        .le(StringUtils.isNotBlank(req.getEndDate()), "created_at", req.getEndDate())
                        .like(StringUtils.isNotBlank(req.getOrderNo()), "order_no", req.getOrderNo())
                        .eq(req.getOrderType() !=null , "order_type", req.getOrderType())
                        .like(req.getWarehouseId() != null, "warehouse_id", req.getWarehouseId())
                        .like(req.getSupplierId() != null, "supplier_id", req.getSupplierId())
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", 0)
                        .orderByAsc("created_by")
        );
    }
}