package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.OutboundOrder;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.enums.CkCommonEnums;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkOutboundOrderMapper;
import com.example.service.CkOutboundOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

// CkOutboundOrderServiceImpl.java
@Service
public class CkOutboundOrderServiceImpl extends ServiceImpl<CkOutboundOrderMapper, OutboundOrder> implements CkOutboundOrderService {

    @Override
    public List<OutboundOrder> selectByProductIds(Long tenantId, List<Long> productIds, Integer status) {
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .in("product_id", productIds)
                        .eq("status", status)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public Long selectCountsOfApprovals(Long tenantId) {
        return baseMapper.selectCount(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .eq("status", CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode())
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
        );
    }

    @Override
    public Long selectCountsOfOutboundOrders(Long tenantId, Date date) {
        return baseMapper.selectCount(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                        .gt("created_at", date)
        );
    }

    @Override
    public List<OutboundOrder> selectCountsByInboundListPageReq(OutboundListPageReq req) {
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrder>()
                        .eq(req.getWarehouseId()!= null, "warehouse_id", req.getWarehouseId())
                        .eq(req.getCustomerId()!=null, "customer_id", req.getCustomerId())
                        .eq(req.getStatus()!=null, "status", req.getStatus())
                        .eq(req.getOrderType()!=null, "order_type", req.getOrderType())
                        .gt(req.getStartDate()!=null, "created_at", req.getStartDate())
                        .lt(req.getEndDate()!=null, "created_at", req.getEndDate())
                        .like(StringUtils.isNotBlank(req.getOrderNo()), "order_no", req.getOrderNo())
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                        .orderByDesc("created_at")
        );
    }

    @Override
    public List<OutboundOrder> selectByOutboundOrderNos(Long tenantId, List<String> outboundOrderNoList) {
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .in("order_no", outboundOrderNoList)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public List<OutboundOrder> selectByOutboundOrderIds(Long tenantId, List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .in("id", orderIds)
                        .eq("is_deleted", 0)
        );
    }

    @Override
    public OutboundOrder selectById(Long id, Long tenantId) {
        return baseMapper.selectOne(new QueryWrapper<OutboundOrder>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode()));
    }

    @Override
    public Page<OutboundOrder> getPage(Page<OutboundOrder> page, OutboundListPageReq req) {
        return baseMapper.selectPage(page,
                new QueryWrapper<OutboundOrder>()
                        .eq(req.getWarehouseId()!= null, "warehouse_id", req.getWarehouseId())
                        .eq(req.getCustomerId()!=null, "customer_id", req.getCustomerId())
                        .eq(req.getStatus()!=null, "status", req.getStatus())
                        .eq(req.getOrderType()!=null, "order_type", req.getOrderType())
                        .gt(req.getStartDate()!=null, "created_at", req.getStartDate())
                        .lt(req.getEndDate()!=null, "created_at", req.getEndDate())
                        .like(StringUtils.isNotBlank(req.getOrderNo()), "order_no", req.getOrderNo())
                        .eq("tenant_id", req.getTenantId())
                        .eq("is_deleted", CkCommonEnums.IsDeleted.NoDelete.getCode())
                        .orderByDesc("created_at")
        );
    }

    @Override
    public List<OutboundOrder> selectByInboundOrderIds(Long tenantId, List<Long> outboundOrderIds) {
        if (outboundOrderIds == null || outboundOrderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(
                new QueryWrapper<OutboundOrder>()
                        .eq("tenant_id", tenantId)
                        .in("id", outboundOrderIds)
                        .eq("is_deleted", 0)
        );
    }
}