package com.example.holder;

import cn.hutool.core.date.DateUtil;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.BomAllocationCreateReq;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.req.OutboundCreateSaleProductReq;
import com.example.entity.cangku.resp.LockResult;
import com.example.enums.CkInOutboundEnums;
import com.example.service.*;
import com.example.utils.SnowflakeIdWorkerUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 库存锁定服务
 */
@Slf4j
@Service
public class CkInventoryLockService {

    @Resource
    private CkStockLockService stockLockService;

    @Resource
    private CkStockLockLogService stockLockLogService;

    @Resource
    private CkInventoryBatchService inventoryBatchService;

    @Resource
    private CkInventoryShelfService inventoryShelfService;

    @Resource
    private CkInventoryWarehouseService inventoryWarehouseService;

    @Resource
    private CkOutboundOrderService outboundOrderService;

    @Resource
    private CkOutboundOrderItemService outboundOrderItemService;

    @Resource
    private SnowflakeIdWorkerUtil snowflakeIdWorkerUtil;

    /**
     * 创建销售出库单时锁定库存
     */
    @Transactional(rollbackFor = Exception.class)
    public LockResult lockForSaleOutbound(OutboundCreateSaleProductReq req, Long orderId) {
        log.info("开始锁定销售出库单库存, 订单ID: {}, 租户ID: {}", orderId, req.getTenantId());

        LockResult result = LockResult.builder().build();
        result.setSuccess(true);
        result.setLockNo("LOCK-" + System.currentTimeMillis());
        result.setOrderId(orderId);
        result.setLockTime(new Date());

        try {
            List<OutboundCreateSaleProductReq.OrderItemInner> items = req.getItems();
            if (CollectionUtils.isEmpty(items)) {
                result.setSuccess(false);
                result.setMessage("出库单明细为空");
                return result;
            }

            List<LockResult.LockItemDetail> lockItems = new ArrayList<>();
            List<LockResult.LockFailureItem> failures = new ArrayList<>();

            // 逐个商品锁定库存
            for (int i = 0; i < items.size(); i++) {
                OutboundCreateSaleProductReq.OrderItemInner item = items.get(i);

                // 校验批次分配
                if (CollectionUtils.isEmpty(item.getBatchAllocations())) {
                    failures.add(LockResult.LockFailureItem.builder()
                            .productId(item.getProductId())
                            .planQuantity(item.getQuantity())
                            .reason("未分配批次")
                            .build());
                    continue;
                }

                // 对每个批次进行锁定
                for (OutboundCreateSaleProductReq.BatchAllocationInner batchAlloc : item.getBatchAllocations()) {
                    try {
                        // 创建锁定记录
                        StockLock stockLock = createStockLockForSaleOutbound(
                                req, orderId, item, batchAlloc);

                        // 更新批次库存的锁定数量
                        boolean updateSuccess = updateBatchLockedQuantity(
                                req.getTenantId(),
                                req.getWarehouseId(),
                                item.getProductId(),
                                batchAlloc.getBatchNo(),
                                batchAlloc.getShelfId(),
                                batchAlloc.getQuantity(),
                                true); // 锁定

                        if (!updateSuccess) {
                            throw new RuntimeException("更新批次锁定数量失败");
                        }

                        // 记录锁定明细
                        lockItems.add(LockResult.LockItemDetail.builder()
                                .productId(item.getProductId())
                                .batchNo(batchAlloc.getBatchNo())
                                .shelfId(batchAlloc.getShelfId())
                                .planQuantity(batchAlloc.getQuantity())
                                .lockedQuantity(batchAlloc.getQuantity())
                                .lockId(stockLock.getId())
                                .build());

                    } catch (Exception e) {
                        log.error("锁定库存失败, productId: {}, batchNo: {}",
                                item.getProductId(), batchAlloc.getBatchNo(), e);
                        failures.add(LockResult.LockFailureItem.builder()
                                .productId(item.getProductId())
                                .batchNo(batchAlloc.getBatchNo())
                                .planQuantity(batchAlloc.getQuantity())
                                .reason(e.getMessage())
                                .build());
                    }
                }
            }

            // 如果有锁定失败的项目
            if (CollectionUtils.isNotEmpty(failures)) {
                result.setSuccess(false);
                result.setMessage("部分库存锁定失败");
                result.setFailureItems(failures);

                // 回滚已锁定的库存
                rollbackLocks(lockItems, req.getTenantId(), req.getWarehouseId());
            }

            result.setLockItems(lockItems);
            result.setLockCount(lockItems.size());

        } catch (Exception e) {
            log.error("销售出库单锁定库存异常", e);
            result.setSuccess(false);
            result.setMessage("库存锁定异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建生产领料出库单时锁定库存
     */
    @Transactional(rollbackFor = Exception.class)
    public LockResult lockForProductionOutbound(OutboundCreateReq req, Long orderId) {
        log.info("开始锁定生产领料出库单库存, 订单ID: {}, 租户ID: {}", orderId, req.getTenantId());

        LockResult result = LockResult.builder().build();
        result.setSuccess(true);
        result.setLockNo("LOCK-" + System.currentTimeMillis());
        result.setOrderId(orderId);
        result.setLockTime(new Date());

        try {
            List<OutboundCreateReq.ProductInfoInner> items = req.getItems();
            if (CollectionUtils.isEmpty(items)) {
                result.setSuccess(false);
                result.setMessage("出库单明细为空");
                return result;
            }

            List<LockResult.LockItemDetail> lockItems = new ArrayList<>();
            List<LockResult.LockFailureItem> failures = new ArrayList<>();

            // 处理每个产品的BOM分配
            for (OutboundCreateReq.ProductInfoInner item : items) {
                if (CollectionUtils.isEmpty(item.getBomAllocations())) {
                    failures.add(LockResult.LockFailureItem.builder()
                            .productId(item.getProductId())
                            .planQuantity(item.getQuantity())
                            .reason("未分配BOM原料")
                            .build());
                    continue;
                }

                // 锁定每个BOM组件的库存
                for (BomAllocationCreateReq bomAlloc : item.getBomAllocations()) {
                    try {
                        // 校验库存可用性
                        validateInventoryAvailable(
                                req.getTenantId(),
                                req.getWarehouseId(),
                                bomAlloc.getComponentProductId(),
                                bomAlloc.getBatchNo(),
                                bomAlloc.getShelfId(),
                                bomAlloc.getQuantity());

                        // 创建锁定记录
                        StockLock stockLock = createStockLockForProductionOutbound(
                                req, orderId, item, bomAlloc);

                        // 更新批次库存的锁定数量
                        boolean updateSuccess = updateBatchLockedQuantity(
                                req.getTenantId(),
                                req.getWarehouseId(),
                                bomAlloc.getComponentProductId(),
                                bomAlloc.getBatchNo(),
                                bomAlloc.getShelfId(),
                                bomAlloc.getQuantity(),
                                true); // 锁定

                        if (!updateSuccess) {
                            throw new RuntimeException("更新批次锁定数量失败");
                        }

                        // 记录锁定明细
                        lockItems.add(LockResult.LockItemDetail.builder()
                                .productId(bomAlloc.getComponentProductId())
                                .relationProductId(item.getProductId())
                                .batchNo(bomAlloc.getBatchNo())
                                .shelfId(bomAlloc.getShelfId())
                                .planQuantity(bomAlloc.getQuantity())
                                .lockedQuantity(bomAlloc.getQuantity())
                                .lockId(stockLock.getId())
                                .build());

                    } catch (Exception e) {
                        log.error("锁定BOM组件库存失败, componentProductId: {}, batchNo: {}",
                                bomAlloc.getComponentProductId(), bomAlloc.getBatchNo(), e);
                        failures.add(LockResult.LockFailureItem.builder()
                                .productId(bomAlloc.getComponentProductId())
                                .batchNo(bomAlloc.getBatchNo())
                                .planQuantity(bomAlloc.getQuantity())
                                .reason(e.getMessage())
                                .build());
                    }
                }
            }

            // 如果有锁定失败的项目
            if (CollectionUtils.isNotEmpty(failures)) {
                result.setSuccess(false);
                result.setMessage("部分库存锁定失败");
                result.setFailureItems(failures);

                // 回滚已锁定的库存
                rollbackLocks(lockItems, req.getTenantId(), req.getWarehouseId());
            }

            result.setLockItems(lockItems);
            result.setLockCount(lockItems.size());

        } catch (Exception e) {
            log.error("生产领料出库单锁定库存异常", e);
            result.setSuccess(false);
            result.setMessage("库存锁定异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 解锁库存（出库单审核通过时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public LockResult unlockForOutboundApproval(OutboundApproveOkReq approveOkReq) {
        log.info("开始解锁出库单库存, 订单ID: {}, 租户ID: {}",
                approveOkReq.getId(), approveOkReq.getTenantId());

        LockResult result = LockResult.builder().build();
        result.setSuccess(true);
        result.setOrderId(approveOkReq.getId());
        result.setUnlockTime(new Date());

        try {
            // 查询出库单
            OutboundOrder outboundOrder = outboundOrderService.getById(approveOkReq.getId());
            if (outboundOrder == null) {
                result.setSuccess(false);
                result.setMessage("出库单不存在");
                return result;
            }

            // 查询出库单明细
            List<OutboundOrderItem> orderItems = outboundOrderItemService
                    .selectByOrderId(approveOkReq.getId(), approveOkReq.getTenantId());
            if (CollectionUtils.isEmpty(orderItems)) {
                result.setSuccess(false);
                result.setMessage("出库单明细为空");
                return result;
            }

            // 根据订单类型查询对应的锁定记录
            List<StockLock> locks;
            if (CkInOutboundEnums.OutBoundType.SaleOutbound.getCode().equals(outboundOrder.getOrderType())) {
                locks = stockLockService.findBySourceIdAndLockType(
                        approveOkReq.getTenantId(),
                        approveOkReq.getId(),
                        CkInOutboundEnums.InventoryLockType.SALES_OUTBOUND.getCode());
            } else if (CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode().equals(outboundOrder.getOrderType())) {
                locks = stockLockService.findBySourceIdAndLockType(
                        approveOkReq.getTenantId(),
                        approveOkReq.getId(),
                        CkInOutboundEnums.InventoryLockType.PRODUCTION_OUTBOUND.getCode());
            } else {
                result.setSuccess(false);
                result.setMessage("不支持的出库类型: " + outboundOrder.getOrderType());
                return result;
            }

            if (CollectionUtils.isEmpty(locks)) {
                log.warn("未找到对应的库存锁定记录, 订单ID: {}", approveOkReq.getId());
                result.setSuccess(true);
                result.setMessage("未找到库存锁定记录，可能未锁定或已解锁");
                return result;
            }

            List<LockResult.LockItemDetail> unlockItems = new ArrayList<>();

            // 解锁每个锁定记录
            for (StockLock lock : locks) {
                try {
                    // 计算需要解锁的数量（根据订单明细实际数量）
                    BigDecimal unlockQuantity = calculateUnlockQuantity(lock, orderItems);
                    if (unlockQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    // 更新锁定记录状态
                    StockLock updatedLock = updateLockForUnlock(lock, unlockQuantity, approveOkReq.getUserId());

                    // 更新批次库存的锁定数量
                    boolean updateSuccess = updateBatchLockedQuantity(
                            lock.getTenantId(),
                            lock.getWarehouseId(),
                            lock.getProductId(),
                            lock.getBatchNo(),
                            lock.getShelfId(),
                            unlockQuantity,
                            false); // 解锁

                    if (!updateSuccess) {
                        throw new RuntimeException("更新批次锁定数量失败");
                    }

                    // 记录解锁操作日志
                    createUnlockLog(updatedLock, unlockQuantity, approveOkReq.getUserId(), "出库审核通过");

                    unlockItems.add(LockResult.LockItemDetail.builder()
                            .productId(lock.getProductId())
                            .batchNo(lock.getBatchNo())
                            .shelfId(lock.getShelfId())
                            .planQuantity(lock.getLockQuantity())
                            .lockedQuantity(unlockQuantity)
                            .lockId(lock.getId())
                            .build());

                } catch (Exception e) {
                    log.error("解锁库存失败, lockId: {}, productId: {}",
                            lock.getId(), lock.getProductId(), e);
                    result.setSuccess(false);
                    result.setMessage("部分库存解锁失败: " + e.getMessage());
                }
            }

            result.setUnlockItems(unlockItems);
            result.setUnlockCount(unlockItems.size());

        } catch (Exception e) {
            log.error("解锁出库单库存异常", e);
            result.setSuccess(false);
            result.setMessage("库存解锁异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 取消出库单时释放锁定库存
     */
    @Transactional(rollbackFor = Exception.class)
    public LockResult unlockForOutboundCancel(Long orderId, Long tenantId, Long userId) {
        log.info("开始释放出库单锁定库存（取消）, 订单ID: {}, 租户ID: {}", orderId, tenantId);

        LockResult result = LockResult.builder().build();
        result.setSuccess(true);
        result.setOrderId(orderId);
        result.setUnlockTime(new Date());

        try {
            // 查询所有相关的锁定记录
            List<StockLock> locks = stockLockService.findBySourceId(tenantId, orderId);
            if (CollectionUtils.isEmpty(locks)) {
                result.setSuccess(true);
                result.setMessage("未找到库存锁定记录");
                return result;
            }

            List<LockResult.LockItemDetail> unlockItems = new ArrayList<>();

            // 强制释放所有锁定
            for (StockLock lock : locks) {
                try {
                    // 如果锁定还没完全解锁，需要强制释放
                    if (!Objects.equals(lock.getLockStatus(), CkInOutboundEnums.InventoryLockStatus.FULLY_UNLOCKED.getCode())) {
                        // 更新锁定记录为强制释放
                        StockLock updatedLock = forceReleaseLock(lock, userId);

                        // 更新批次库存的锁定数量（完全释放）
                        boolean updateSuccess = updateBatchLockedQuantity(
                                lock.getTenantId(),
                                lock.getWarehouseId(),
                                lock.getProductId(),
                                lock.getBatchNo(),
                                lock.getShelfId(),
                                lock.getAvailableLockQuantity(),
                                false); // 解锁

                        if (!updateSuccess) {
                            throw new RuntimeException("更新批次锁定数量失败");
                        }

                        // 记录强制释放日志
                        createForceReleaseLog(updatedLock, userId, "出库单取消");

                        unlockItems.add(LockResult.LockItemDetail.builder()
                                .productId(lock.getProductId())
                                .batchNo(lock.getBatchNo())
                                .shelfId(lock.getShelfId())
                                .planQuantity(lock.getLockQuantity())
                                .lockedQuantity(lock.getAvailableLockQuantity())
                                .lockId(lock.getId())
                                .build());
                    }

                } catch (Exception e) {
                    log.error("强制释放库存失败, lockId: {}, productId: {}",
                            lock.getId(), lock.getProductId(), e);
                    result.setSuccess(false);
                    result.setMessage("部分库存释放失败: " + e.getMessage());
                }
            }

            result.setUnlockItems(unlockItems);
            result.setUnlockCount(unlockItems.size());

        } catch (Exception e) {
            log.error("释放出库单锁定库存异常", e);
            result.setSuccess(false);
            result.setMessage("库存释放异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 编辑出库单时重新锁定库存（先释放旧锁定，再创建新锁定）
     */
    @Transactional(rollbackFor = Exception.class)
    public LockResult relockForOutboundUpdate(Long orderId, Object updateReq, Long tenantId, Long userId, Integer orderType) {
        log.info("开始重新锁定出库单库存（编辑）, 订单ID: {}, 租户ID: {}", orderId, tenantId);

        // 1. 先释放原有锁定
        LockResult unlockResult = unlockForOutboundCancel(orderId, tenantId, userId);
        if (!unlockResult.getSuccess()) {
            log.warn("释放原有库存锁定失败, 订单ID: {}", orderId);
            // 继续尝试新锁定，因为可能是部分失败
        }

        // 2. 根据订单类型创建新锁定
        LockResult lockResult = LockResult.builder().build();
        lockResult.setOrderId(orderId);

        try {
            if (CkInOutboundEnums.OutBoundType.SaleOutbound.getCode().equals(orderType)) {
                if (updateReq instanceof OutboundCreateSaleProductReq) {
                    lockResult = lockForSaleOutbound((OutboundCreateSaleProductReq) updateReq, orderId);
                }
            } else if (CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode().equals(orderType)) {
                if (updateReq instanceof OutboundCreateReq) {
                    lockResult = lockForProductionOutbound((OutboundCreateReq) updateReq, orderId);
                }
            } else {
                lockResult.setSuccess(false);
                lockResult.setMessage("不支持的出库类型: " + orderType);
            }
        } catch (Exception e) {
            log.error("重新锁定库存异常", e);
            lockResult.setSuccess(false);
            lockResult.setMessage("重新锁定库存异常: " + e.getMessage());

            // 如果新锁定失败，尝试恢复原有锁定
            if (unlockResult.getSuccess() && CollectionUtils.isNotEmpty(unlockResult.getUnlockItems())) {
                log.info("新锁定失败，尝试恢复原有锁定");
                restoreLocks(unlockResult.getUnlockItems(), tenantId, userId);
            }
        }

        return lockResult;
    }

    /**
     * 查询可用库存（排除锁定部分）
     */
    public BigDecimal getAvailableQuantity(Long tenantId, Long warehouseId, Long productId,
                                          String batchNo, Long shelfId) {
        try {
            // 查询总库存
            InventoryShelf inventoryShelf = inventoryShelfService.getByTenantWarehouseProductShelfBatch(
                    tenantId, warehouseId, productId, shelfId, batchNo);

            if (inventoryShelf == null) {
                return BigDecimal.ZERO;
            }

            // 查询锁定数量
            BigDecimal lockedQuantity = stockLockService.sumLockedQuantity(
                    tenantId, warehouseId, productId, batchNo, shelfId,
                    CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode());

            // 可用数量 = 总数量 - 锁定数量
            return inventoryShelf.getQuantity().subtract(lockedQuantity);

        } catch (Exception e) {
            log.error("查询可用库存异常", e);
            return BigDecimal.ZERO;
        }
    }

    // ============ 私有方法 ============

    /**
     * 创建销售出库锁定记录
     */
    private StockLock createStockLockForSaleOutbound(OutboundCreateSaleProductReq req,
                                                    Long orderId,
                                                    OutboundCreateSaleProductReq.OrderItemInner item,
                                                    OutboundCreateSaleProductReq.BatchAllocationInner batchAlloc) {

        StockLock stockLock = new StockLock();
        stockLock.setId(snowflakeIdWorkerUtil.nextId());
        stockLock.setTenantId(req.getTenantId());
        stockLock.setLockType(CkInOutboundEnums.InventoryLockType.SALES_OUTBOUND.getCode());
        stockLock.setLockSource("order");
        stockLock.setSourceId(orderId);
        stockLock.setSourceNo(req.getOrderNo());

        stockLock.setProductId(item.getProductId());
        stockLock.setWarehouseId(req.getWarehouseId());
        stockLock.setBatchNo(batchAlloc.getBatchNo());
        stockLock.setShelfId(batchAlloc.getShelfId());

        stockLock.setLockQuantity(batchAlloc.getQuantity());
        stockLock.setUnlockQuantity(BigDecimal.ZERO);
        stockLock.setLockStatus(CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode());
        stockLock.setLockStrategy(1); // FIFO策略

        stockLock.setLockPurpose(CkInOutboundEnums.LockPurpose.SALES_OCCUPY.getCode());
        stockLock.setLockDirection(CkInOutboundEnums.LockDirection.OUTBOUND_LOCK.getCode());

        // 设置过期时间（默认24小时）
        stockLock.setExpireTime(DateUtil.offsetHour(new Date(), 24));
        stockLock.setExpectedUnlockTime(DateUtil.parseDate(req.getExpectedDate()));

        stockLock.setPriority(5); // 中等优先级
        stockLock.setIsPreemptable(1); // 可被抢占

        stockLock.setLockReason("销售出库单锁定");
        stockLock.setCreatedBy(req.getUserId());
        stockLock.setModifiedBy(req.getUserId());
        stockLock.setCreatedAt(new Date());
        stockLock.setModifiedAt(new Date());
        stockLock.setIsDeleted(0);

        stockLockService.save(stockLock);

        // 记录锁定日志
        createLockLog(stockLock, req.getUserId(), "销售出库单创建锁定");

        return stockLock;
    }

    /**
     * 创建生产领料出库锁定记录
     */
    private StockLock createStockLockForProductionOutbound(OutboundCreateReq req,
                                                          Long orderId,
                                                          OutboundCreateReq.ProductInfoInner item,
                                                          BomAllocationCreateReq bomAlloc) {

        StockLock stockLock = new StockLock();
        stockLock.setId(snowflakeIdWorkerUtil.nextId());
        stockLock.setTenantId(req.getTenantId());
        stockLock.setLockType(CkInOutboundEnums.InventoryLockType.PRODUCTION_OUTBOUND.getCode());
        stockLock.setLockSource("order");
        stockLock.setSourceId(orderId);
        stockLock.setSourceNo(req.getOrderNo());

        stockLock.setProductId(bomAlloc.getComponentProductId());
        stockLock.setWarehouseId(req.getWarehouseId());
        stockLock.setBatchNo(bomAlloc.getBatchNo());
        stockLock.setShelfId(bomAlloc.getShelfId());

        stockLock.setLockQuantity(bomAlloc.getQuantity());
        stockLock.setUnlockQuantity(BigDecimal.ZERO);
        stockLock.setLockStatus(CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode());
        stockLock.setLockStrategy(1); // FIFO策略

        stockLock.setLockPurpose(CkInOutboundEnums.LockPurpose.PRODUCTION_OCCUPY.getCode());
        stockLock.setLockDirection(CkInOutboundEnums.LockDirection.OUTBOUND_LOCK.getCode());

        // 关联成品ID
        Map<String, Object> extData = new HashMap<>();
        extData.put("relationProductId", item.getProductId());
        extData.put("relationProductQuantity", item.getQuantity());
        // stockLock.setExtData(JSON.toJSONString(extData)); // 如果有JSON字段

        // 设置过期时间（默认48小时，生产领料可能需要更长时间）
        stockLock.setExpireTime(DateUtil.offsetHour(new Date(), 48));
        stockLock.setExpectedUnlockTime(DateUtil.parseDate(req.getExpectedDate()));

        stockLock.setPriority(3); // 生产领料优先级较高
        stockLock.setIsPreemptable(0); // 生产领料不可被抢占

        stockLock.setLockReason("生产领料出库单锁定");
        stockLock.setCreatedBy(req.getUserId());
        stockLock.setModifiedBy(req.getUserId());
        stockLock.setCreatedAt(new Date());
        stockLock.setModifiedAt(new Date());
        stockLock.setIsDeleted(0);

        stockLockService.save(stockLock);

        // 记录锁定日志
        createLockLog(stockLock, req.getUserId(), "生产领料出库单创建锁定");

        return stockLock;
    }

    /**
     * 更新批次库存的锁定数量
     */
    private boolean updateBatchLockedQuantity(Long tenantId, Long warehouseId, Long productId,
                                             String batchNo, Long shelfId, BigDecimal quantity,
                                             boolean isLock) {
        try {
            // 更新货架库存表的锁定数量
            InventoryShelf inventoryShelf = inventoryShelfService.getByTenantWarehouseProductShelfBatch(
                    tenantId, warehouseId, productId, shelfId, batchNo);

            if (inventoryShelf == null) {
                log.error("货架库存记录不存在, productId: {}, shelfId: {}, batchNo: {}",
                        productId, shelfId, batchNo);
                return false;
            }

            BigDecimal newLockedQuantity;
            if (isLock) {
                // 锁定：增加锁定数量
                newLockedQuantity = inventoryShelf.getLockedQuantity().add(quantity);

                // 校验可用库存是否足够
                BigDecimal availableQuantity = inventoryShelf.getQuantity()
                        .subtract(inventoryShelf.getLockedQuantity());
                if (availableQuantity.compareTo(quantity) < 0) {
                    throw new RuntimeException("可用库存不足，可用:" + availableQuantity + "，需求:" + quantity);
                }
            } else {
                // 解锁：减少锁定数量
                newLockedQuantity = inventoryShelf.getLockedQuantity().subtract(quantity);
                if (newLockedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    newLockedQuantity = BigDecimal.ZERO;
                }
            }

            inventoryShelf.setLockedQuantity(newLockedQuantity);
            inventoryShelf.setModifiedAt(new Date());

            return inventoryShelfService.updateById(inventoryShelf);

        } catch (Exception e) {
            log.error("更新批次锁定数量失败", e);
            throw new RuntimeException("更新批次锁定数量失败: " + e.getMessage());
        }
    }

    /**
     * 校验库存可用性
     */
    private void validateInventoryAvailable(Long tenantId, Long warehouseId, Long productId,
                                           String batchNo, Long shelfId, BigDecimal requiredQuantity) {

        InventoryShelf inventoryShelf = inventoryShelfService.getByTenantWarehouseProductShelfBatch(
                tenantId, warehouseId, productId, shelfId, batchNo);

        if (inventoryShelf == null) {
            throw new RuntimeException("库存记录不存在");
        }

        // 可用库存 = 总数量 - 锁定数量
        BigDecimal availableQuantity = inventoryShelf.getQuantity()
                .subtract(inventoryShelf.getLockedQuantity());

        if (availableQuantity.compareTo(requiredQuantity) < 0) {
            throw new RuntimeException(String.format("库存不足，可用: %s，需求: %s",
                    availableQuantity, requiredQuantity));
        }
    }

    /**
     * 更新锁定记录（解锁时）
     */
    private StockLock updateLockForUnlock(StockLock lock, BigDecimal unlockQuantity, Long userId) {
        BigDecimal newUnlockQuantity = lock.getUnlockQuantity().add(unlockQuantity);

        // 判断锁定状态
        Integer newLockStatus;
        if (newUnlockQuantity.compareTo(lock.getLockQuantity()) >= 0) {
            newLockStatus = CkInOutboundEnums.InventoryLockStatus.FULLY_UNLOCKED.getCode();
        } else if (newUnlockQuantity.compareTo(BigDecimal.ZERO) > 0) {
            newLockStatus = CkInOutboundEnums.InventoryLockStatus.PARTIALLY_UNLOCKED.getCode();
        } else {
            newLockStatus = lock.getLockStatus();
        }

        lock.setUnlockQuantity(newUnlockQuantity);
        lock.setLockStatus(newLockStatus);
        lock.setActualUnlockTime(new Date());
        lock.setModifiedBy(userId);
        lock.setModifiedAt(new Date());

        stockLockService.updateById(lock);

        return lock;
    }

    /**
     * 强制释放锁定
     */
    private StockLock forceReleaseLock(StockLock lock, Long userId) {
        lock.setLockStatus(CkInOutboundEnums.InventoryLockStatus.FORCE_RELEASED.getCode());
        lock.setActualUnlockTime(new Date());
        lock.setModifiedBy(userId);
        lock.setModifiedAt(new Date());

        stockLockService.updateById(lock);

        return lock;
    }

    /**
     * 计算需要解锁的数量
     */
    private BigDecimal calculateUnlockQuantity(StockLock lock, List<OutboundOrderItem> orderItems) {
        // 找出对应的订单明细
        Optional<OutboundOrderItem> matchedItem = orderItems.stream()
                .filter(item -> item.getProductId().equals(lock.getProductId())
                        && StringUtils.equals(item.getBatchNo(), lock.getBatchNo())
                        && Objects.equals(item.getShelfLocationId(), lock.getShelfId()))
                .findFirst();

        if (matchedItem.isPresent()) {
            // 解锁订单明细中实际出库的数量
            return matchedItem.get().getQuantity();
        } else {
            // 如果没有找到对应的订单明细，解锁全部
            return lock.getAvailableLockQuantity();
        }
    }

    /**
     * 回滚锁定
     */
    private void rollbackLocks(List<LockResult.LockItemDetail> lockItems, Long tenantId, Long warehouseId) {
        for (LockResult.LockItemDetail item : lockItems) {
            try {
                // 恢复批次库存的锁定数量
                updateBatchLockedQuantity(
                        tenantId,
                        warehouseId,
                        item.getProductId(),
                        item.getBatchNo(),
                        item.getShelfId(),
                        item.getLockedQuantity(),
                        false); // 解锁（回滚）

                // 删除锁定记录
                stockLockService.removeById(item.getLockId());

            } catch (Exception e) {
                log.error("回滚锁定失败, lockId: {}", item.getLockId(), e);
            }
        }
    }

    /**
     * 恢复锁定
     */
    private void restoreLocks(List<LockResult.LockItemDetail> unlockItems, Long tenantId, Long userId) {
        for (LockResult.LockItemDetail item : unlockItems) {
            try {
                // 重新锁定库存
                updateBatchLockedQuantity(
                        tenantId,
                        null, // warehouseId需要从原锁定记录获取
                        item.getProductId(),
                        item.getBatchNo(),
                        item.getShelfId(),
                        item.getLockedQuantity(),
                        true); // 锁定

                // 恢复锁定记录状态
                StockLock lock = stockLockService.getById(item.getLockId());
                if (lock != null) {
                    lock.setLockStatus(CkInOutboundEnums.InventoryLockStatus.LOCKED.getCode());
                    lock.setUnlockQuantity(BigDecimal.ZERO);
                    lock.setModifiedBy(userId);
                    lock.setModifiedAt(new Date());
                    stockLockService.updateById(lock);
                }

            } catch (Exception e) {
                log.error("恢复锁定失败, lockId: {}", item.getLockId(), e);
            }
        }
    }

    /**
     * 创建锁定日志
     */
    private void createLockLog(StockLock lock, Long userId, String operationReason) {
        StockLockLog log = new StockLockLog();
        log.setId(snowflakeIdWorkerUtil.nextId());
        log.setTenantId(lock.getTenantId());
        log.setLockId(lock.getId());
        log.setOperationType(1); // 创建锁定
        log.setOperationSource("system");
        log.setOperationReason(operationReason);

        log.setBeforeLockQuantity(BigDecimal.ZERO);
        log.setBeforeUnlockQuantity(BigDecimal.ZERO);
        log.setChangeLockQuantity(lock.getLockQuantity());
        log.setChangeUnlockQuantity(BigDecimal.ZERO);

        log.setBeforeLockStatus(0); // 锁定前状态为0
        log.setAfterLockStatus(lock.getLockStatus());

        log.setRelatedOrderId(lock.getSourceId());
        log.setRelatedOrderType(lock.getLockType());

        log.setCreatedBy(userId);
        log.setModifiedBy(userId);
        log.setCreatedAt(new Date());
        log.setModifiedAt(new Date());
        log.setIsDeleted(0);

        stockLockLogService.save(log);
    }

    /**
     * 创建解锁日志
     */
    private void createUnlockLog(StockLock lock, BigDecimal unlockQuantity, Long userId, String reason) {
        StockLockLog log = new StockLockLog();
        log.setId(snowflakeIdWorkerUtil.nextId());
        log.setTenantId(lock.getTenantId());
        log.setLockId(lock.getId());
        log.setOperationType(2); // 解锁
        log.setOperationSource("system");
        log.setOperationReason(reason);

        log.setBeforeLockQuantity(lock.getLockQuantity());
        log.setBeforeUnlockQuantity(lock.getUnlockQuantity().subtract(unlockQuantity));
        log.setChangeLockQuantity(BigDecimal.ZERO);
        log.setChangeUnlockQuantity(unlockQuantity);

        log.setBeforeLockStatus(lock.getLockStatus());
        log.setAfterLockStatus(lock.getLockStatus());

        log.setRelatedOrderId(lock.getSourceId());
        log.setRelatedOrderType(lock.getLockType());

        log.setCreatedBy(userId);
        log.setModifiedBy(userId);
        log.setCreatedAt(new Date());
        log.setModifiedAt(new Date());
        log.setIsDeleted(0);

        stockLockLogService.save(log);
    }

    /**
     * 创建强制释放日志
     */
    private void createForceReleaseLog(StockLock lock, Long userId, String reason) {
        StockLockLog log = new StockLockLog();
        log.setId(snowflakeIdWorkerUtil.nextId());
        log.setTenantId(lock.getTenantId());
        log.setLockId(lock.getId());
        log.setOperationType(4); // 强制释放
        log.setOperationSource("system");
        log.setOperationReason(reason);

        log.setBeforeLockQuantity(lock.getLockQuantity());
        log.setBeforeUnlockQuantity(lock.getUnlockQuantity());
        log.setChangeLockQuantity(BigDecimal.ZERO);
        log.setChangeUnlockQuantity(lock.getAvailableLockQuantity());

        log.setBeforeLockStatus(lock.getLockStatus());
        log.setAfterLockStatus(lock.getLockStatus());

        log.setRelatedOrderId(lock.getSourceId());
        log.setRelatedOrderType(lock.getLockType());

        log.setCreatedBy(userId);
        log.setModifiedBy(userId);
        log.setCreatedAt(new Date());
        log.setModifiedAt(new Date());
        log.setIsDeleted(0);

        stockLockLogService.save(log);
    }
}