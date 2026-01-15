package com.example.holder;

import com.example.entity.cangku.dto.*;
import com.example.service.CkInventoryBatchService;
import com.example.service.CkInventoryService;
import com.example.service.CkInventoryShelfService;
import com.example.service.CkInventoryWarehouseService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存更新帮助类
 * 使用位置
 * 1、2026-01-15 在调整单执行的时候，调整库存的代码中使用
 */
@Component
@Slf4j
public class InventoryUpdateHelper {
    
    @Resource
    private CkInventoryService inventoryService;
    
    @Resource
    private CkInventoryWarehouseService inventoryWarehouseService;
    
    @Resource
    private CkInventoryBatchService inventoryBatchService;
    
    @Resource
    private CkInventoryShelfService inventoryShelfService;
    
    /**
     * 根据调整单明细更新库存
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean updateInventoryByAdjustItem(Long tenantId, AdjustOrderItem item) {
        Long productId = item.getProductId();
        Long warehouseId = item.getWarehouseId();
        String batchNo = item.getBatchNo();
        Long shelfId = item.getShelfId();
        BigDecimal adjustQuantity = item.getAdjustQuantity();
        
        try {
            // 1. 更新库存总表
            updateInventory(tenantId, productId, adjustQuantity);
            
            // 2. 更新仓库库存
            updateWarehouseInventory(tenantId, productId, warehouseId, adjustQuantity);
            
            // 3. 更新批次库存（如果批次号不为空）
            if (StringUtils.isNotBlank(batchNo)) {
                updateBatchInventory(tenantId, productId, warehouseId, batchNo, adjustQuantity);
            }
            
            // 4. 更新货架库存（如果货架ID不为空）
            if (shelfId != null && shelfId > 0) {
                updateShelfInventory(tenantId, productId, warehouseId, shelfId, batchNo, adjustQuantity);
            }
            
            return true;
        } catch (ValidationException e) {
            log.error("更新库存失败，tenantId:{}, productId:{}, warehouseId:{}, quantity:{}", 
                    tenantId, productId, warehouseId, adjustQuantity, e);
            throw e;
        }catch (Exception e) {
            log.error("更新库存失败，tenantId:{}, productId:{}, warehouseId:{}, quantity:{}",
                    tenantId, productId, warehouseId, adjustQuantity, e);
            throw new RuntimeException("库存更新失败", e);
        }
    }
    
    private void updateInventory(Long tenantId, Long productId, BigDecimal changeQuantity) {
        Inventory inventory = inventoryService.getByProduct(productId, tenantId);
        
        if (inventory == null) {
            // 如果库存不存在，创建新的库存记录
            inventory = new Inventory();
            inventory.setTenantId(tenantId);
            inventory.setProductId(productId);
            inventory.setQuantity(changeQuantity.compareTo(BigDecimal.ZERO) > 0 ? changeQuantity : BigDecimal.ZERO);
            inventory.setLockedQuantity(BigDecimal.ZERO);
            inventory.setCreatedBy(0L);
            inventory.setCreatedAt(new Date());
            inventory.setModifiedBy(0L);
            inventory.setModifiedAt(new Date());
            inventory.setIsDeleted(0);
            inventoryService.save(inventory);
        } else {
            // 更新现有库存
            BigDecimal newQuantity = inventory.getQuantity().add(changeQuantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("库存数量不能为负数，productId:" + productId);
            }
            
            inventory.setQuantity(newQuantity);
            inventory.setModifiedAt(new Date());
            inventoryService.updateById(inventory);
        }
    }
    
    private void updateWarehouseInventory(Long tenantId, Long productId, Long warehouseId, BigDecimal changeQuantity) {
        InventoryWarehouse warehouseInventory = inventoryWarehouseService.getByWarehouseAndProduct(warehouseId,productId,tenantId);

        if (warehouseInventory == null) {
            warehouseInventory = new InventoryWarehouse();
            warehouseInventory.setTenantId(tenantId);
            warehouseInventory.setProductId(productId);
            warehouseInventory.setWarehouseId(warehouseId);
            warehouseInventory.setQuantity(changeQuantity.compareTo(BigDecimal.ZERO) > 0 ? changeQuantity : BigDecimal.ZERO);
            warehouseInventory.setLockedQuantity(BigDecimal.ZERO);
            warehouseInventory.setCreatedBy(0L);
            warehouseInventory.setCreatedAt(new Date());
            warehouseInventory.setModifiedBy(0L);
            warehouseInventory.setModifiedAt(new Date());
            warehouseInventory.setIsDeleted(0);
            inventoryWarehouseService.save(warehouseInventory);
        } else {
            BigDecimal newQuantity = warehouseInventory.getQuantity().add(changeQuantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("仓库库存数量不能为负数，productId:" + productId + ", warehouseId:" + warehouseId);
            }
            
            warehouseInventory.setQuantity(newQuantity);
            warehouseInventory.setModifiedAt(new Date());
            inventoryWarehouseService.updateById(warehouseInventory);
        }
    }
    
    private void updateBatchInventory(Long tenantId, Long productId, Long warehouseId, 
                                    String batchNo, BigDecimal changeQuantity) {
        InventoryBatch batchInventory = inventoryBatchService.selectByProductBatchWarehouse(tenantId, productId,batchNo, warehouseId);
        
        if (batchInventory == null) {
            batchInventory = new InventoryBatch();
            batchInventory.setTenantId(tenantId);
            batchInventory.setProductId(productId);
            batchInventory.setBatchNo(batchNo);
            batchInventory.setWarehouseId(warehouseId);
            batchInventory.setQuantity(changeQuantity.compareTo(BigDecimal.ZERO) > 0 ? changeQuantity : BigDecimal.ZERO);
            batchInventory.setLockedQuantity(BigDecimal.ZERO);
            batchInventory.setCreatedBy(0L);
            batchInventory.setCreatedAt(new Date());
            batchInventory.setModifiedBy(0L);
            batchInventory.setModifiedAt(new Date());
            batchInventory.setIsDeleted(0);
            inventoryBatchService.save(batchInventory);
        } else {
            BigDecimal newQuantity = batchInventory.getQuantity().add(changeQuantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("批次库存数量不能为负数，productId:" + productId + 
                        ", batchNo:" + batchNo);
            }
            
            batchInventory.setQuantity(newQuantity);
            batchInventory.setModifiedAt(new Date());
            inventoryBatchService.updateById(batchInventory);
        }
    }
    
    private void updateShelfInventory(Long tenantId, Long productId, Long warehouseId, 
                                     Long shelfId, String batchNo, BigDecimal changeQuantity) {
        InventoryShelf shelfInventory = inventoryShelfService
                .getByWarehouseAndProductAndShelf(warehouseId,productId,shelfId, batchNo, tenantId);
        
        if (shelfInventory == null) {
            shelfInventory = new InventoryShelf();
            shelfInventory.setTenantId(tenantId);
            shelfInventory.setProductId(productId);
            shelfInventory.setWarehouseId(warehouseId);
            shelfInventory.setShelfId(shelfId);
            shelfInventory.setBatchNo(batchNo);
            shelfInventory.setQuantity(changeQuantity.compareTo(BigDecimal.ZERO) > 0 ? changeQuantity : BigDecimal.ZERO);
            shelfInventory.setLockedQuantity(BigDecimal.ZERO);
            shelfInventory.setCreatedBy(0L);
            shelfInventory.setCreatedAt(new Date());
            shelfInventory.setModifiedBy(0L);
            shelfInventory.setModifiedAt(new Date());
            shelfInventory.setIsDeleted(0);
            inventoryShelfService.save(shelfInventory);
        } else {
            BigDecimal newQuantity = shelfInventory.getQuantity().add(changeQuantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("货架库存数量不能为负数，productId:" + productId + 
                        ", shelfId:" + shelfId);
            }
            
            shelfInventory.setQuantity(newQuantity);
            shelfInventory.setModifiedAt(new Date());
            inventoryShelfService.updateById(shelfInventory);
        }
    }
}