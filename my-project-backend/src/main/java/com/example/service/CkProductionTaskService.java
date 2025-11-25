package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.ProductionTask;
import com.example.enums.CkInOutboundEnums;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/24 23:18
 */
public interface CkProductionTaskService extends IService<ProductionTask> {
    Integer removeByOutBoundId(Long id, Long tenantId, Long userId);

    List<ProductionTask> selectByOutBoundId(Long orderId, Long tenantId);
    List<ProductionTask> selectByOutBoundIds(List<Long> orderId, Long tenantId);

    List<ProductionTask> selectRemainingQuantityBT0ByStatus(CkInOutboundEnums.ProductionTaskStatus code, Long tenantId);

    Integer updateProductionTaskStatus(Long tenantId, Long outboundId, CkInOutboundEnums.ProductionTaskStatus partialCompletion, Long userId);

    List<ProductionTask> selectByOutboundOrderNos(Long tenantId, List<String> outboundNos);
}
