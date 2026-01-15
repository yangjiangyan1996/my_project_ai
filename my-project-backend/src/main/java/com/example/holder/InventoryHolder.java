package com.example.holder;

import com.example.entity.cangku.dto.*;
import com.example.enums.CkInventoryEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 11:05
 */
@Service
public class InventoryHolder {

    @Resource
    private CkInventoryService inventoryService;
    @Resource
    private CkInventoryBatchService inventoryBatchService;
    @Resource
    private CkInventoryTransactionService inventoryTransactionService;
    @Resource
    private CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    private CkInventoryShelfService inventoryShelfService;

    /**
     * 获取每个productId出库单中ID最大的流水
     */
    public  Map<Long, InventoryTransaction> getLatestOutTransactions(List<InventoryTransaction> inventoryTransactions) {
        if (inventoryTransactions == null || inventoryTransactions.isEmpty()) {
            return Collections.emptyMap();
        }

        // 只保留出库单 orderType = 2
        List<InventoryTransaction> outList = inventoryTransactions.stream()
                .filter(tx -> tx.getOrderType() != null && tx.getOrderType() == 2)
                .collect(Collectors.toList());

        // 使用Map存储每个productId对应ID最大的流水
        Map<Long, InventoryTransaction> productId2LatestInventorySactionMap = new HashMap<>();
        for (InventoryTransaction tx : outList) {
            Long productId = tx.getProductId();
            if (!productId2LatestInventorySactionMap.containsKey(productId) || tx.getId() > productId2LatestInventorySactionMap.get(productId).getId()) {
                productId2LatestInventorySactionMap.put(productId, tx);
            }
        }

        // 返回结果
        return productId2LatestInventorySactionMap;
    }


    /**
     * 审核通过时减少库存
     */
    public void updateSubInventoryForApprove(OutboundOrder outboundOrder, List<OutboundOrderItem> orderItems, Long userId) {
        for (OutboundOrderItem item : orderItems) {
            // 更新库存, 记录库存流水
            updateSubInventory(outboundOrder, item, userId);

            // 更新仓库库存
            updateSubWarehouseInventory(outboundOrder, item, userId);

            //更新货架库存
            updateSubShelfInventory(outboundOrder, item, userId);

            // 创建批次库存
            updateSubInventoryBatch(outboundOrder, item, userId);
        }
    }

    /**
     * 审核通过时新增库存
     *
     * @param inboundOrder
     * @param orderItems
     * @param userId
     */
    public void updateAddInventoryForApprove(InboundOrder inboundOrder, List<InboundOrderItem> orderItems, Long userId) {
        for (InboundOrderItem item : orderItems) {
            // 更新库存, 记录库存流水
            updateAddInventory(inboundOrder, item, userId);

            // 更新仓库库存
            updateAddWarehouseInventory(inboundOrder, item, userId);

            //更新货架库存
            updateAddShelfInventory(inboundOrder, item, userId);

            // 创建库存批次
            updateAddInventoryBatch(inboundOrder, item, userId);
        }
    }


    /**
     * 审核通过时更新库存
     */
    private void updateAddInventory(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), inboundOrder.getTenantId());

        BigDecimal oldQuantity = BigDecimal.ZERO;
        BigDecimal newQuantity = BigDecimal.ZERO;

        if (existingInventory != null) {
            // 更新现有库存
            newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            oldQuantity = existingInventory.getQuantity();


            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            oldQuantity = BigDecimal.ZERO;
            newQuantity = item.getActualQuantity();

            Inventory newInventory = new Inventory();
            newInventory.setProductId(item.getProductId());
            newInventory.setQuantity(newQuantity);
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setTenantId(inboundOrder.getTenantId());
            newInventory.setCreatedBy(userId);
            newInventory.setModifiedBy(userId);
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryService.save(newInventory);
            if (!saved) {
                throw new ValidationException("库存创建失败，产品ID: " + item.getProductId());
            }
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(inboundOrder.getWarehouseId());
        transaction.setBatchNo(item.getBatchNo());
        transaction.setShelfId(item.getShelfLocationId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(CkInventoryEnums.OrderType.IN.getCode()); // 1-入库单
        transaction.setOrderTypeDetail(inboundOrder.getOrderType());
        transaction.setOrderId(inboundOrder.getId());
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getActualQuantity()); // 正数表示增加
        transaction.setBalanceQuantity(newQuantity);
        transaction.setBeforBalanceQuantity(oldQuantity);
        transaction.setTransactionTime(new Date());
        transaction.setTenantId(inboundOrder.getTenantId());
        transaction.setPriceUnit(item.getPriceUnit());
        transaction.setPriceTotal(item.getPriceTotal());
        transaction.setCreatedBy(userId);
        transaction.setModifiedBy(userId);
        transaction.setCreatedAt(new Date());
        transaction.setModifiedAt(new Date());
        transaction.setIsDeleted(0);

        boolean saved = inventoryTransactionService.save(transaction);
        if (!saved) {
            throw new ValidationException("库存流水记录创建失败");
        }
    }


    private void updateSubInventory(OutboundOrder outboundOrder, OutboundOrderItem item, Long userId) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), outboundOrder.getTenantId());

        BigDecimal oldQuantity = BigDecimal.ZERO;
        BigDecimal newQuantity = BigDecimal.ZERO;

        if (existingInventory != null) {
            // 更新现有库存
            newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("库存不足，产品ID: " + item.getProductId());
            }
            oldQuantity = existingInventory.getQuantity();


            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            throw new ValidationException("商品无库存，库存扣减失败，产品ID: " + item.getProductId());
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(outboundOrder.getWarehouseId());
        transaction.setShelfId(item.getShelfLocationId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(CkInventoryEnums.OrderType.OUT.getCode());
        transaction.setOrderTypeDetail(outboundOrder.getOrderType());
        transaction.setOrderId(outboundOrder.getId());
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getQuantity());
        transaction.setBalanceQuantity(newQuantity);
        transaction.setBeforBalanceQuantity(oldQuantity);
        transaction.setTransactionTime(new Date());
        transaction.setTenantId(outboundOrder.getTenantId());
        transaction.setPriceUnit(item.getPriceUnit());
        transaction.setPriceTotal(item.getPriceTotal());
        transaction.setPriceUnitUsd(item.getPriceUnitUsd());
        transaction.setPriceTotalUsd(item.getPriceTotalUsd());
        transaction.setCreatedBy(userId);
        transaction.setModifiedBy(userId);
        transaction.setCreatedAt(new Date());
        transaction.setModifiedAt(new Date());
        transaction.setIsDeleted(0);

        boolean saved = inventoryTransactionService.save(transaction);
        if (!saved) {
            throw new ValidationException("库存流水记录创建失败");
        }
    }

    private void updateAddWarehouseInventory(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryWarehouse existingInventory = inventoryWarehouseService.getByWarehouseAndProduct(inboundOrder.getWarehouseId(), item.getProductId(), inboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryWarehouseService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("仓库库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            InventoryWarehouse newInventory = new InventoryWarehouse();
            newInventory.setProductId(item.getProductId());
            newInventory.setWarehouseId(inboundOrder.getWarehouseId());
            newInventory.setQuantity(item.getActualQuantity());
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setTenantId(inboundOrder.getTenantId());
            newInventory.setCreatedBy(userId);
            newInventory.setModifiedBy(userId);
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryWarehouseService.save(newInventory);
            if (!saved) {
                throw new ValidationException("仓库库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }

    private void updateSubWarehouseInventory(OutboundOrder outboundOrder, OutboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryWarehouse existingInventory = inventoryWarehouseService.getByWarehouseAndProduct(outboundOrder.getWarehouseId(), item.getProductId(), outboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("仓库库存不足，产品ID: " + item.getProductId());
            }
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryWarehouseService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            throw new ValidationException("商品无仓库库存，库存扣减失败，产品ID: " + item.getProductId());
        }
    }


    private void updateAddShelfInventory(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryShelf existingInventory = inventoryShelfService.getByWarehouseAndProductAndShelf(inboundOrder.getWarehouseId(), item.getProductId(), item.getShelfLocationId(), item.getBatchNo(), inboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryShelfService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            InventoryShelf newInventory = new InventoryShelf();
            newInventory.setProductId(item.getProductId());
            newInventory.setBatchNo(item.getBatchNo());
            newInventory.setWarehouseId(inboundOrder.getWarehouseId());
            newInventory.setShelfId(item.getShelfLocationId());
            newInventory.setQuantity(item.getActualQuantity());
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setTenantId(inboundOrder.getTenantId());
            newInventory.setCreatedBy(userId);
            newInventory.setModifiedBy(userId);
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryShelfService.save(newInventory);
            if (!saved) {
                throw new ValidationException("库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }

    private void updateSubShelfInventory(OutboundOrder outboundOrder, OutboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryShelf existingInventory = inventoryShelfService.getByWarehouseAndProductAndShelf(outboundOrder.getWarehouseId(), item.getProductId(), item.getShelfLocationId(), item.getBatchNo(), outboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("货架库存不足，产品ID: " + item.getProductId());
            }
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryShelfService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("货架库存更新失败，产品ID: " + item.getProductId());
            }
        } else {

            throw new ValidationException("商品无货架库存，货架库存扣减失败，产品ID: " + item.getProductId());
        }
    }

    private void updateAddInventoryBatch(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryBatch ib = inventoryBatchService.selectByBatchNoAndProductId(item.getBatchNo(), item.getProductId(), inboundOrder.getTenantId());

        if (ib != null) {
            // 更新现有库存
            BigDecimal newQuantity = ib.getQuantity().add(item.getActualQuantity());
            ib.setQuantity(newQuantity);
            ib.setModifiedBy(userId);
            ib.setModifiedAt(new Date());

            boolean updated = inventoryBatchService.updateById(ib);
            if (!updated) {
                throw new ValidationException("批次库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            InventoryBatch newInventory = new InventoryBatch();
            newInventory.setTenantId(inboundOrder.getTenantId());
            newInventory.setProductId(item.getProductId());
            newInventory.setBatchNo(item.getBatchNo());
            newInventory.setWarehouseId(inboundOrder.getWarehouseId());
            newInventory.setQuantity(item.getActualQuantity());
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setInboundOrderId(item.getOrderId());
            newInventory.setInboundItemId(item.getId());
            newInventory.setProductionDate(new Date());
            newInventory.setCreatedBy(userId);
            newInventory.setModifiedBy(userId);
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryBatchService.save(newInventory);
            if (!saved) {
                throw new ValidationException("批次库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }

    private void updateSubInventoryBatch(OutboundOrder outboundOrder, OutboundOrderItem item, Long userId) {
        if (StringUtils.isEmpty(item.getBatchNo())) {
            return;
        }
        // 查询现有库存
        InventoryBatch ib = inventoryBatchService.selectByBatchNoAndProductId(item.getBatchNo(), item.getProductId(), outboundOrder.getTenantId());

        if (ib != null) {
            // 更新现有库存
            BigDecimal newQuantity = ib.getQuantity().subtract(item.getQuantity());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("批次库存不足，产品ID: " + item.getProductId());
            }
            ib.setQuantity(newQuantity);
            ib.setModifiedBy(userId);
            ib.setModifiedAt(new Date());

            boolean updated = inventoryBatchService.updateById(ib);
            if (!updated) {
                throw new ValidationException("批次库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            throw new ValidationException("商品无批次库存，批次库存扣减失败，产品ID: " + item.getProductId());
        }
    }
}
