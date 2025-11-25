package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductionTask;
import com.example.enums.CkInOutboundEnums;
import com.example.mapper.CkProductionTaskMapper;
import com.example.service.CkProductionTaskService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/24 23:19
 */
@Service
public class CkProductionTaskServiceImpl extends ServiceImpl<CkProductionTaskMapper, ProductionTask> implements CkProductionTaskService {
    @Override
    public Integer removeByOutBoundId(Long outboundOrderId, Long tenantId, Long userId) {
        ProductionTask productionTask = new ProductionTask();
        productionTask.setModifiedBy(userId);
        productionTask.setModifiedAt(new Date());
        productionTask.setIsDeleted(1);
        productionTask.setOutboundOrderId(outboundOrderId);
        return baseMapper.update(productionTask,
                new QueryWrapper<ProductionTask>()
                        .eq("outbound_order_id", outboundOrderId)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0));
    }

    @Override
    public List<ProductionTask> selectByOutboundOrderNos(Long tenantId, List<String> outboundNos) {
        return baseMapper.selectList(
                new QueryWrapper<ProductionTask>()
                        .in("outbound_order_no", outboundNos)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0));
    }

    @Override
    public Integer updateProductionTaskStatus(Long tenantId, Long outboundId, CkInOutboundEnums.ProductionTaskStatus partialCompletion, Long userId) {
        ProductionTask productionTask = new ProductionTask();
        productionTask.setStatus(partialCompletion.getCode());
        productionTask.setModifiedBy(userId);
        productionTask.setModifiedAt(new Date());
        return baseMapper.update(productionTask,
                new QueryWrapper<ProductionTask>()
                        .eq("outbound_order_id", outboundId)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0));
    }

    @Override
    public List<ProductionTask> selectRemainingQuantityBT0ByStatus(CkInOutboundEnums.ProductionTaskStatus code, Long tenantId) {
        return baseMapper.selectList(
                new QueryWrapper<ProductionTask>()
                        .eq("status", code.getCode())
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0)
                        .gt("remaining_quantity", 0));
    }

    @Override
    public List<ProductionTask> selectByOutBoundIds(List<Long> orderId, Long tenantId) {
        return baseMapper.selectList(
                new QueryWrapper<ProductionTask>()
                        .in("outbound_order_id", orderId)
                        .eq("tenant_id", tenantId)
                        .eq("status", CkInOutboundEnums.ProductionTaskStatus.PartialCompletion.getCode())
                        .eq("is_deleted", 0));
    }

    @Override
    public List<ProductionTask> selectByOutBoundId(Long orderId, Long tenantId) {
        return baseMapper.selectList(
                new QueryWrapper<ProductionTask>()
                        .eq("outbound_order_id", orderId)
                        .eq("tenant_id", tenantId)
                        .eq("is_deleted", 0));
    }
}
