package com.example.Facade;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.AdjustOrder;
import com.example.entity.cangku.dto.AdjustOrderItem;
import com.example.entity.cangku.dto.InventoryTransaction;
import com.example.entity.cangku.req.AdjustApproveOkReq;
import com.example.entity.cangku.req.AdjustListPageReq;
import com.example.entity.cangku.req.AdjustRequest;
import com.example.entity.cangku.req.AdjustSubmitApproveReq;
import com.example.entity.cangku.resp.AdjustOrderResp;
import com.example.enums.CkAdjustEnums;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkInventoryEnums;
import com.example.holder.InventoryUpdateHelper;
import com.example.service.CkAdjustOrderItemService;
import com.example.service.CkAdjustOrderService;
import com.example.service.CkInventoryTransactionService;
import com.example.utils.DateUtils;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:23
 */
@Slf4j
@Service
public class CkAdjustOrderFacade {
    @Resource
    CkAdjustOrderService adjustOrderService;
    @Resource
    CkInventoryTransactionService inventoryTransactionService;
    @Resource
    InventoryUpdateHelper inventoryUpdateHelper;
    @Resource
    CkAdjustOrderItemService adjustOrderItemService;


    @Transactional(rollbackFor = Exception.class)
    public Boolean create(AdjustRequest req) {
        // 1. 生成调整单号（如果前端未提供）
        if (req.getAdjustNo() == null || req.getAdjustNo().trim().isEmpty()) {
            String adjustNo = generateAdjustNo(req.getTenantId());
            req.setAdjustNo(adjustNo);
        }

        // 2. 计算统计信息
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (AdjustRequest.AdjustItemRequest item : req.getItems()) {
            // 计算调整数量的绝对值
            BigDecimal absQuantity = item.getAdjustQuantity().abs();
            totalQuantity = totalQuantity.add(absQuantity);

            // 计算调整成本金额
            if (item.getUnitCost() != null) {
                BigDecimal costAmount = item.getAdjustQuantity().multiply(item.getUnitCost());
                totalCostAmount = totalCostAmount.add(costAmount);
            }

            // 计算调整金额
            if (item.getUnitPrice() != null) {
                BigDecimal amount = item.getAdjustQuantity().multiply(item.getUnitPrice());
                totalAmount = totalAmount.add(amount);
            }
        }

        // 3. 保存调整单主表
        AdjustOrder order = new AdjustOrder();
        order.setAdjustNo(req.getAdjustNo());
        order.setWarehouseId(req.getWarehouseId());
        order.setAdjustType(req.getAdjustType());
        order.setSourceType(req.getSourceType());
        order.setSourceId(req.getSourceId());
        order.setSourceNo(req.getSourceNo());
        order.setAdjustReason(req.getAdjustReason());
        order.setIsAffectCost(req.getIsAffectCost());
        order.setIsUrgent(req.getIsUrgent());
        order.setPriority(req.getPriority() != null ? req.getPriority() : 3); // 默认中优先级
        order.setRemark(req.getRemark());

        // 设置状态信息
        order.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.WaitSubmit.getCode()); // 待提交

        // 设置统计信息
        order.setTotalItems(req.getItems().size());
        order.setTotalQuantity(totalQuantity);
        order.setTotalCostAmount(totalCostAmount);
        order.setTotalAmount(totalAmount);

        // 设置时间信息
        order.setExpectExecuteTime(DateUtils.str2Date(req.getExpectExecuteTime()));

        //设置审核信息
        order.setApproverId(req.getApprovalUserId());

        // 设置基础信息
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedBy(req.getUserId());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setVersion(0);

        // 保存主表
        boolean save = adjustOrderService.save(order);
        if (!save) {
            throw new ValidationException("保存调整单失败");
        }

        // 4. 保存调整单明细
        List<AdjustOrderItem> items = req.getItems().stream().map(itemReq -> {
            AdjustOrderItem item = new AdjustOrderItem();

            // 基础信息
            item.setAdjustOrderId(order.getId());
            item.setAdjustNo(order.getAdjustNo());
            item.setTenantId(req.getTenantId());

            // 产品信息
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setProductCode(itemReq.getProductCode());
            item.setSkuCode(itemReq.getSkuCode());
            item.setSpecification(itemReq.getSpec());
            item.setUnit(itemReq.getUnit());

            // 库存信息
            item.setWarehouseId(req.getWarehouseId());
            item.setWarehouseName(req.getWarehouseName());
            item.setBatchNo(itemReq.getBatchNo());
            item.setShelfId(itemReq.getShelfId());
            item.setShelfCode(itemReq.getShelfCode());
            item.setLocationCode(itemReq.getLocationCode());

            // 数量信息
            item.setBeforeQuantity(itemReq.getBeforeQuantity());
            item.setAdjustQuantity(itemReq.getAdjustQuantity());
            item.setAfterQuantity(itemReq.getAfterQuantity());

            // 成本金额信息
            item.setUnitCost(itemReq.getUnitCost());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setAdjustCostAmount(itemReq.getAdjustCostAmount());
            item.setAdjustAmount(itemReq.getAdjustAmount());

            // 计算成本金额和调整金额（如果未提供）
            if (item.getAdjustCostAmount() == null && item.getUnitCost() != null) {
                BigDecimal costAmount = item.getAdjustQuantity().multiply(item.getUnitCost());
                item.setAdjustCostAmount(costAmount);
            }

            if (item.getAdjustAmount() == null && item.getUnitPrice() != null) {
                BigDecimal amount = item.getAdjustQuantity().multiply(item.getUnitPrice());
                item.setAdjustAmount(amount);
            }

            // 其他信息
            item.setAdjustReason(itemReq.getItemReason() != null ? itemReq.getItemReason() : req.getAdjustReason());
            item.setItemRemark(itemReq.getItemRemark());
            item.setInventoryType(itemReq.getInventoryType() != null ? itemReq.getInventoryType() : 1);
            item.setIsAffectCost(req.getIsAffectCost());
            item.setCostAdjustMethod(itemReq.getCostAdjustMethod() != null ? itemReq.getCostAdjustMethod() : 1);
            item.setSourceItemId(itemReq.getSourceItemId());
            item.setStatus(1); // 待执行
            item.setExtData(itemReq.getExtData());

            // 基础字段
            item.setCreatedBy(req.getUserId());
            item.setCreatedAt(new Date());
            item.setModifiedBy(req.getUserId());
            item.setModifiedAt(new Date());
            item.setIsDeleted(0);

            return item;
        }).collect(Collectors.toList());

        // 批量保存明细
        boolean saveBatch = adjustOrderItemService.saveBatch(items);
        if (!saveBatch) {
            throw new ValidationException("保存调整单明细失败");
        }

        return true;
    }

    /**
     * 生成调整单号
     */
    private String generateAdjustNo(Long tenantId) {
        // 示例：TZ202512250001
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        // 这里需要查询当天的最大流水号，这里简化为示例
        String sequence = String.format("%04d", 1); // 实际应该从数据库获取
        return "TZ" + dateStr + sequence;
    }

    public Page<AdjustOrderResp> pageList(Page<AdjustOrder> page, AdjustListPageReq req) {
        Page<AdjustOrder> list = adjustOrderService.getStockPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

//        Map<Long, List<StockTakeItem>> stockTakeId2ItemListMap = new HashMap<>();
//        List<Long> stockTakeIds = list.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(stockTakeIds)) {
//            List<StockTakeItem> itemList = stockTaskItemService.selectByStockTakeIds(stockTakeIds, req.getTenantId());
//            stockTakeId2ItemListMap = itemList.stream().collect(Collectors.groupingBy(StockTakeItem::getStockTakeId));
//        }
//
//        Map<Long, Account> userId2AccountMap = new HashMap<>();
//        List<Long> userIds = list.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(userIds)) {
//            List<Account> accounts = accountService.selectByIds(userIds);
//            userId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
//        }
//
//        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
//        List<Long> warehouseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).distinct().collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(warehouseIds)) {
//            List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
//            warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
//        }
//
//        Map<Long, List<StockTakeItem>> finalStockTakeId2ItemListMap = stockTakeId2ItemListMap;
//        Map<Long, Account> finalUserId2AccountMap = userId2AccountMap;
//        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        List<AdjustOrderResp> collect = list.getRecords().stream().map(v -> {
            AdjustOrderResp p = new AdjustOrderResp();
            BeanUtils.copyProperties(v, p);


//            List<StockTakeItem> items = finalStockTakeId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>());
//            p.setItemCount(CollectionUtils.isEmpty(items) ? 0 : items.size());
//
//            List<StockTakeItem> collect1 = items.stream().filter(s -> !CkStockTakeEnums.StockItemStatus.NOT_ADJUSTED.getCode().equals(s.getStatus())).collect(Collectors.toList());
//            p.setCountedCount(collect1.size());
//
//            List<StockTakeItem> diffList = items.stream().filter(s -> s.getDiffQuantity() != null && s.getDiffQuantity().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
//            p.setTotalDiff(diffList.size());
//
//            p.setCreatedByName(finalUserId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
//            p.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            return p;
        }).collect(Collectors.toList());

        Page<AdjustOrderResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean submitApprove(AdjustSubmitApproveReq req) {
        AdjustOrder adjustOrder = adjustOrderService.selectById(req.getId(), req.getTenantId());
        if (adjustOrder == null) {
            throw new ValidationException("调整单不存在");
        }
        if (!CkAdjustEnums.AdjustOrderStatus.WaitSubmit.getCode().equals(adjustOrder.getAdjustStatus())) {
            throw new ValidationException("调整单状态错误");
        }
        adjustOrder.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode());
        adjustOrder.setModifiedBy(req.getUserId());
        adjustOrder.setModifiedAt(new Date());
        boolean updated = adjustOrderService.updateStatusById(adjustOrder.getId(), adjustOrder.getTenantId(), CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode(), req.getUserId());
        if (!updated) {
            throw new ValidationException("请求审核调整单失败");
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOk(AdjustApproveOkReq req) {
        AdjustOrder adjustOrder = adjustOrderService.selectById(req.getId(), req.getTenantId());
        if (adjustOrder == null) {
            throw new ValidationException("调整单不存在");
        }
        if (!CkAdjustEnums.AdjustOrderStatus.WaitAudit.getCode().equals(adjustOrder.getAdjustStatus())) {
            throw new ValidationException("调整单状态错误");
        }
        if (!(CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode().equals(req.getApproveStatus()) || CkAdjustEnums.AdjustOrderStatus.Reject.getCode().equals(req.getApproveStatus()))) {
            throw new ValidationException("提交状态错误");
        }
        adjustOrder.setApproveTime(new Date());
        adjustOrder.setApproveRemark(req.getApproveRemark());
        adjustOrder.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode());
        adjustOrder.setModifiedBy(req.getUserId());
        adjustOrder.setModifiedAt(new Date());
        boolean updated = adjustOrderService.updateStatusAndRemarkById(adjustOrder.getId(), adjustOrder.getTenantId(), CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode(), req.getApproveRemark());
        if (!updated) {
            throw new ValidationException("审核调整单失败");
        }
        return true;
    }


    /**
     * 执行调整单
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(Long id, Long userId, Long tenantId) {
        log.info("开始执行调整单，id:{}, userId:{}, tenantId:{}", id, userId, tenantId);

        // 1. 查询调整单并检查状态
        AdjustOrder adjustOrder = adjustOrderService.selectById(id, tenantId);
        if (adjustOrder == null) {
            throw new ValidationException("调整单不存在");
        }

        // 检查状态：必须是审核通过状态
        if (!CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode().equals(adjustOrder.getAdjustStatus())) {
            // 如果是开始执行状态，可能正在执行中，检查是否超时
            if (CkAdjustEnums.AdjustOrderStatus.StartExecute.getCode().equals(adjustOrder.getAdjustStatus())) {
                // 检查是否超时（超过30分钟视为超时）
                Date now = new Date();
                long diffMinutes = (now.getTime() - adjustOrder.getModifiedAt().getTime()) / (1000 * 60);
                if (diffMinutes < 30) {
                    throw new ValidationException("调整单正在执行中，请稍后重试");
                } else {
                    log.warn("调整单执行超时，重置为审核通过状态，id:{}", id);
                    adjustOrder.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode());
                    adjustOrder.setModifiedBy(userId);
                    adjustOrder.setModifiedAt(now);
                    adjustOrderService.updateById(adjustOrder);
                }
            } else {
                throw new ValidationException("调整单状态错误，当前状态:" + adjustOrder.getAdjustStatus());
            }
        }

        // 2. 获取调整单明细
        List<AdjustOrderItem> adjustOrderItems = adjustOrderItemService.selectByAdjustOrderId(adjustOrder.getId(), tenantId);
        if (CollectionUtils.isEmpty(adjustOrderItems)) {
            throw new ValidationException("调整单无明细");
        }

        // 3. 更新调整单状态为开始执行（乐观锁控制）
        boolean updateToExecuteStatus = updateOrderToExecuteStatus(adjustOrder, userId);
        if (!updateToExecuteStatus) {
            throw new ValidationException("更新调整单状态失败，可能已被其他用户操作");
        }

        try {
            // 4. 准备库存流水记录
            List<InventoryTransaction> transactionList = new ArrayList<>();
            Date executeTime = new Date();

            for (AdjustOrderItem item : adjustOrderItems) {
                // 检查明细状态
                if (!CkAdjustEnums.AdjustOrderItemStatus.WaitExecute.getCode().equals(item.getStatus())) {
                    if (CkAdjustEnums.AdjustOrderItemStatus.Executed.getCode().equals(item.getStatus())) {
                        log.warn("调整单明细已执行，跳过，itemId:{}", item.getId());
                        continue;
                    }
                    throw new ValidationException("调整单明细状态错误，itemId:" + item.getId());
                }

                // 5. 更新库存
                boolean inventoryUpdated = inventoryUpdateHelper.updateInventoryByAdjustItem(tenantId, item);
                if (!inventoryUpdated) {
                    throw new RuntimeException("更新库存失败，itemId:" + item.getId());
                }

                // 6. 创建库存流水记录
                InventoryTransaction transaction = buildInventoryTransaction(adjustOrder, item, executeTime, userId);
                transactionList.add(transaction);

                // 7. 更新明细状态为已执行
                updateItemToExecuted(item, userId, executeTime);
            }

            // 8. 批量插入库存流水
            if (!CollectionUtils.isEmpty(transactionList)) {
                inventoryTransactionService.saveBatch(transactionList);
            }

            // 9. 更新调整单状态为调整完成
            updateOrderToCompleted(adjustOrder, userId, executeTime);

            log.info("调整单执行成功，id:{}, 共处理{}条明细", id, adjustOrderItems.size());
            return true;

        } catch (Exception e) {
            log.error("调整单执行失败，id:{}", id, e);

            // 更新调整单状态为调整失败
            updateOrderToFailed(adjustOrder, userId, e.getMessage());

            // 抛异常让事务回滚
            throw new RuntimeException("调整单执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新调整单状态为开始执行
     */
    private boolean updateOrderToExecuteStatus(AdjustOrder order, Long userId) {
        AdjustOrder updateEntity = new AdjustOrder();
        updateEntity.setId(order.getId());
        updateEntity.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.StartExecute.getCode());
        updateEntity.setModifiedBy(userId);
        updateEntity.setModifiedAt(new Date());
        updateEntity.setVersion(order.getVersion()); // 乐观锁

        return adjustOrderService.update(updateEntity, new QueryWrapper<AdjustOrder>()
                .eq("id", order.getId())
                .eq("tenant_id", order.getTenantId())
                .eq("version", order.getVersion())
                .eq("is_deleted", 0));
    }

    /**
     * 构建库存流水记录
     */
    private InventoryTransaction buildInventoryTransaction(AdjustOrder order, AdjustOrderItem item,
                                                           Date executeTime, Long userId) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTenantId(order.getTenantId());
        //如果变动前的数量 > 变动后的数量，则为出库
        transaction.setOrderType(item.getBeforeQuantity().compareTo(item.getAfterQuantity()) > 0 ? CkInventoryEnums.OrderType.OUT.getCode() : CkInventoryEnums.OrderType.IN.getCode());
        //TODO yang  setOrderTypeDetail ()
        // CkInOutboundEnums#InBoundType
        // 这个看看要不要把出入库的类型处理成一个枚举，或者保证int值不相同
        transaction.setOrderTypeDetail(CkInOutboundEnums.InBoundType.TransferInbound.getCode());
        transaction.setProductId(item.getProductId());
        transaction.setWarehouseId(item.getWarehouseId());
        transaction.setBatchNo(item.getBatchNo());
        transaction.setShelfId(item.getShelfId());
        transaction.setBeforBalanceQuantity(item.getBeforeQuantity());
        transaction.setChangeQuantity(item.getAdjustQuantity());
        transaction.setBalanceQuantity(item.getAfterQuantity());

        // 成本信息
        transaction.setPriceUnit(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
        if (item.getUnitCost() != null
                && !Objects.equals(item.getUnitCost(), BigDecimal.ZERO)
                && item.getAdjustQuantity() != null
                && !Objects.equals(item.getAdjustQuantity(), BigDecimal.ZERO)) {
            transaction.setPriceTotal(item.getUnitCost().multiply(item.getAdjustQuantity()));
        } else {
            transaction.setPriceTotal(BigDecimal.ZERO);
        }

        transaction.setOrderId(order.getId());
        transaction.setOrderItemId(item.getId());
        transaction.setTransactionTime(executeTime);
        transaction.setRemark("库存调整单:" + order.getAdjustNo() + " - " + item.getAdjustReason());
        transaction.setCreatedBy(userId);
        transaction.setCreatedAt(new Date());
        transaction.setModifiedBy(userId);
        transaction.setModifiedAt(new Date());
        transaction.setIsDeleted(0);

        return transaction;
    }

    /**
     * 更新明细状态为已执行
     */
    private void updateItemToExecuted(AdjustOrderItem item, Long userId, Date executeTime) {
        AdjustOrderItem updateItem = new AdjustOrderItem();
        updateItem.setId(item.getId());
        updateItem.setStatus(CkAdjustEnums.AdjustOrderItemStatus.Executed.getCode());
        updateItem.setExecuteTime(executeTime);
        updateItem.setExecuteBy(userId);
        updateItem.setModifiedBy(userId);
        updateItem.setModifiedAt(new Date());

        boolean updated = adjustOrderItemService.update(updateItem, new QueryWrapper<AdjustOrderItem>()
                .eq("id", item.getId())
                .eq("tenant_id", item.getTenantId())
                .eq("status", CkAdjustEnums.AdjustOrderItemStatus.WaitExecute.getCode())
                .eq("is_deleted", 0));

        if (!updated) {
            throw new RuntimeException("更新调整单明细状态失败，itemId:" + item.getId());
        }
    }

    /**
     * 更新调整单状态为调整完成
     */
    private void updateOrderToCompleted(AdjustOrder order, Long userId, Date executeTime) {
        AdjustOrder updateEntity = new AdjustOrder();
        updateEntity.setId(order.getId());
        updateEntity.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.AdjustComplete.getCode());
        updateEntity.setActualExecuteTime(executeTime);
        updateEntity.setModifiedBy(userId);
        updateEntity.setModifiedAt(new Date());

        boolean updated = adjustOrderService.update(updateEntity, new QueryWrapper<AdjustOrder>()
                .eq("id", order.getId())
                .eq("tenant_id", order.getTenantId())
                .eq("adjust_status", CkAdjustEnums.AdjustOrderStatus.StartExecute.getCode())
                .eq("is_deleted", 0));

        if (!updated) {
            throw new RuntimeException("更新调整单状态为完成失败");
        }
    }

    /**
     * 更新调整单状态为调整失败
     */
    private void updateOrderToFailed(AdjustOrder order, Long userId, String errorMsg) {
        try {
            AdjustOrder updateEntity = new AdjustOrder();
            updateEntity.setId(order.getId());
            updateEntity.setAdjustStatus(CkAdjustEnums.AdjustOrderStatus.AdjustFail.getCode());
            updateEntity.setRemark((order.getRemark() != null ? order.getRemark() + "; " : "") +
                    "执行失败:" + (errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg));
            updateEntity.setModifiedBy(userId);
            updateEntity.setModifiedAt(new Date());

            adjustOrderService.update(updateEntity, new QueryWrapper<AdjustOrder>()
                    .eq("id", order.getId())
                    .eq("tenant_id", order.getTenantId())
                    .in("adjust_status",
                            CkAdjustEnums.AdjustOrderStatus.StartExecute.getCode(),
                            CkAdjustEnums.AdjustOrderStatus.AuditPass.getCode())
                    .eq("is_deleted", 0));
        } catch (Exception e) {
            log.error("更新调整单为失败状态异常", e);
        }
    }
}
