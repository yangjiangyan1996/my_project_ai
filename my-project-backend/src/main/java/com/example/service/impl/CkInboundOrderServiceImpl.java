package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.InboundOrder;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkInboundOrderMapper;
import com.example.service.CkInboundOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

// CkInboundOrderServiceImpl.java
@Service
public class CkInboundOrderServiceImpl extends ServiceImpl<CkInboundOrderMapper, InboundOrder> implements CkInboundOrderService {

    @Override
    public Long selectCountsOfApprovals(Long tenantId) {
        return baseMapper.selectCount(new QueryWrapper<InboundOrder>()
                .eq("status", CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode())
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
    }

    @Override
    public Long selectCountsOfInboundOrders(Long tenantId, Date date) {
        return baseMapper.selectCount(new QueryWrapper<InboundOrder>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
                .ge(date != null, "created_at", date));
    }

    @Override
    public List<InboundOrder> selectCountsByInboundListPageReq(InboundListPageReq req) {
        return baseMapper.selectList(new QueryWrapper<InboundOrder>()
                .eq(req.getStatus() != null, "status", req.getStatus())
                .ge(StringUtils.isNotBlank(req.getStartDate()), "created_at", req.getStartDate())
                .le(StringUtils.isNotBlank(req.getEndDate()), "created_at", req.getEndDate())
                .like(StringUtils.isNotBlank(req.getOrderNo()), "order_no", req.getOrderNo())
                .like(StringUtils.isNotBlank(req.getRelatedOrderNo()), "order_no", req.getOrderNo())
                .eq(req.getOrderType() !=null , "order_type", req.getOrderType())
                .like(req.getWarehouseId() != null, "warehouse_id", req.getWarehouseId())
                .like(req.getSupplierId() != null, "supplier_id", req.getSupplierId())
                .eq("tenant_id", req.getTenantId())
                .eq("is_deleted", 0)
                .orderByDesc("created_at"));
    }

    @Override
    public List<InboundOrder> selectByInboundOrderIds(Long tenantId, List<Long> inboundOrderIds) {
        if (inboundOrderIds == null || inboundOrderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.query()
                .eq("tenant_id", tenantId)
                .in("id", inboundOrderIds)
                .eq("is_deleted", 0)
                .list();
    }

    @Override
    public InboundOrder selectById(Long orderId, Long tenantId) {
        return this.query()
                .eq("id", orderId)
                .eq("tenant_id", tenantId)
                .one();
    }

    @Override
    public Page<InboundOrder> getPage(Page<InboundOrder> page, InboundListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<InboundOrder>()
                        .eq(req.getStatus() != null, "status", req.getStatus())
                        .ge(StringUtils.isNotBlank(req.getStartDate()), "created_at", req.getStartDate())
                        .le(StringUtils.isNotBlank(req.getEndDate()), "created_at", req.getEndDate())
                        .like(StringUtils.isNotBlank(req.getOrderNo()), "order_no", req.getOrderNo())
                        .like(StringUtils.isNotBlank(req.getRelatedOrderNo()), "order_no", req.getOrderNo())
                        .eq(req.getOrderType() !=null , "order_type", req.getOrderType())
                        .like(req.getWarehouseId() != null, "warehouse_id", req.getWarehouseId())
                        .like(req.getSupplierId() != null, "supplier_id", req.getSupplierId())
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", 0)
                        .orderByDesc("created_at")
        );
    }
}