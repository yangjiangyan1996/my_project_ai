package com.example.holder;

import com.example.entity.cangku.dto.InboundOrderItem;
import com.example.entity.cangku.dto.ProductionTask;
import com.example.entity.cangku.req.InboundCreateReq;
import com.example.enums.CkInOutboundEnums;
import com.example.service.CkProductionTaskService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/26 23:33
 */
@Slf4j
@Service
public class ProductTaskHolder {

    @Resource
    private CkProductionTaskService productionTaskService;

    public  boolean checkProductTaskAndSave (InboundCreateReq req, List<InboundOrderItem> orderItemsSave) {
        //判断数量生产入库数量是否足够
        //后续要加入审核的时候，要加一个锁定库存
        if (req.getOrderType().equals(CkInOutboundEnums.InBoundType.ProductionInbound.getCode())) {
            List<String> outboundNos = orderItemsSave.stream().map(v -> v.getRelatedOutboundOrderNo()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(outboundNos)) { // 手动创建不关联生产领料的时候，outboundNos 会存在空的情况
                List<ProductionTask> productionTasks = productionTaskService.selectByOutboundOrderNos(req.getTenantId(), outboundNos);
                Map<String, Map<Long, ProductionTask>> outboundNo2ProductId2QuantityMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(productionTasks)) {
                    outboundNo2ProductId2QuantityMap = productionTasks.stream().collect(Collectors.groupingBy(ProductionTask::getOutboundOrderNo, Collectors.toMap(ProductionTask::getProductId, v -> v)));
                }
                List<ProductionTask> productionTasksToUpdate = new ArrayList<>();
                for (InboundCreateReq.InboundDetailCreateReq item : req.getItems()) {
                    if (outboundNo2ProductId2QuantityMap.containsKey(item.getRelatedPickingOrderNo())) {
                        Map<Long, ProductionTask> productId2QuantityOfTaskMap = outboundNo2ProductId2QuantityMap.getOrDefault(item.getRelatedPickingOrderNo(), new HashMap<>());
                        ProductionTask pt = productId2QuantityOfTaskMap.getOrDefault(item.getProductId(), new ProductionTask());
                        if (pt.getRemainingQuantity().compareTo(new BigDecimal(item.getActualQuantity())) < 0) {
                            throw new ValidationException("生产领料余额不足，生产领料剩余：" + pt.getRemainingQuantity() + "，实际入库数量：" + item.getActualQuantity());
                        } else {
                            ProductionTask updatePt = new ProductionTask();
                            updatePt.setId(pt.getId());
                            updatePt.setRemainingQuantity(pt.getRemainingQuantity().subtract(new BigDecimal(item.getActualQuantity())));
                            updatePt.setLockQuantity(pt.getLockQuantity().add(new BigDecimal(item.getActualQuantity())));
                            productionTasksToUpdate.add(updatePt);
                        }
                    }
                }
                boolean productionTasksUpdated = productionTaskService.updateBatchById(productionTasksToUpdate);
                if (!productionTasksUpdated) {
                    throw new ValidationException("生产任务更新失败");
                }
            }
        }
        return true;
    }
}
