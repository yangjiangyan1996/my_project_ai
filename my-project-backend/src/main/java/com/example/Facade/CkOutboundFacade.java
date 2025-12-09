package com.example.Facade;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.req.excel.OutBoundSaleQuantityImportDto;
import com.example.entity.cangku.resp.*;
import com.example.entity.cangku.resp.excel.OutboundOderExcelModel;
import com.example.entity.cangku.resp.excel.OutboundSaleExcelModel;
import com.example.entity.dto.Account;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkProductEnums;
import com.example.holder.InventoryHolder;
import com.example.service.*;
import com.example.utils.ExcelUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:04
 */
@Service
@Slf4j
public class CkOutboundFacade {

    @Resource
    CkProductionTaskService productionTaskService;
    @Resource
    CkOutboundOrderItemSaleExtService outboundOrderItemSaleExtService;
    @Resource
    CkOutboundOrderItemService outboundOrderItemService;

    @Resource
    CkOutboundOrderService outboundOrderService;

    @Resource
    CkProductBomService productBomService;
    @Resource
    CkProductBomDetailService productBomDetailService;
    @Resource
    CkProductService productService;
    @Resource
    CkShelfService shelfService;
    @Resource
    CkInventoryService inventoryService;
    @Resource
    InventoryHolder inventoryHolder;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryTransactionService inventoryTransactionService;
    @Resource
    CkCustomerService customerService;
    @Resource
    CkUnitService unitService;
    @Resource
    AccountService accountService;
    @Resource
    CkInventoryFacade inventoryFacade;
    @Resource
    CkWareHouseService warehouseService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OutboundCreateReq req) {
        // 1. 参数校验
        validateUpdateReq(req);

        // 2. 查询现有出库单
        OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
        if (existingOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        // 3. 校验状态：只有草稿和已拒绝状态可以编辑
        if (existingOrder.getStatus() != 0 && existingOrder.getStatus() != 4) {
            throw new ValidationException("只有草稿和已拒绝状态的出库单可以编辑");
        }

        // 4. 构建更新后的出库单主表实体
        OutboundOrder updatedOrder = buildUpdatedOutboundOrder(req, existingOrder);

        // 5. 更新出库单主表
        boolean orderUpdated = outboundOrderService.updateById(updatedOrder);
        if (!orderUpdated) {
            throw new ValidationException("出库单主表更新失败");
        }

        // 6. 删除原有的出库单明细
        int itemsDeleted = outboundOrderItemService.deleteByOrderId(req.getId(), req.getTenantId(), req.getUserId());
        if (itemsDeleted < 0) {
            throw new ValidationException("原有出库单明细删除失败");
        }

        // 7. 插入新的出库单明细
        List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, req.getId());
        boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        //删除原有的生产任务
        Integer integer = productionTaskService.removeByOutBoundId(updatedOrder.getId(), req.getTenantId(), req.getUserId());
        if (integer > 0) {
            //处理生产任务关联信息
            List<ProductionTask> batchSaveProductTaskList = req.getItems().stream().map(v -> {
                ProductionTask pt = new ProductionTask();
                pt.setTenantId(req.getTenantId());
                pt.setTaskNo("productionTask-" + UUID.randomUUID().toString());
                pt.setOutboundOrderId(updatedOrder.getId());
                pt.setOutboundOrderNo(updatedOrder.getOrderNo());
                pt.setProductId(v.getProductId());
                //pt.setProductName(v.getProductName());
                pt.setPlannedQuantity(v.getQuantity());
                pt.setMaterialQuantity(v.getQuantity());
                pt.setProducedQuantity(BigDecimal.ZERO);
                pt.setRemainingQuantity(v.getQuantity());
                pt.setLockQuantity(BigDecimal.ZERO);
                pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
                pt.setWarehouseId(req.getWarehouseId());
                pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
                pt.setRemark(v.getRemark());
                pt.setCreatedAt(new Date());
                pt.setCreatedBy(req.getUserId());
                pt.setModifiedAt(new Date());
                pt.setModifiedBy(req.getUserId());
                return pt;
            }).collect(Collectors.toList());
            boolean saveBatchProductionTaskResult = productionTaskService.saveBatch(batchSaveProductTaskList);
            if (!saveBatchProductionTaskResult) {
                throw new ValidationException("生产任务保存失败");
            }
        }


        // 8. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            inventoryHolder.updateSubInventoryForApprove(updatedOrder, orderItems, req.getUserId());
            //updateInventoryAndTransaction(req, req.getId(), orderItems);
        }

        return true;
    }

    /**
     * 更新参数校验
     */
    private void validateUpdateReq(OutboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("出库单更新请求不能为空");
        }

        // ID校验
        if (req.getId() == null) {
            throw new ValidationException("出库单ID不能为空");
        }

        // 基础字段校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        }
        if (req.getOrderType() == null || req.getOrderType() < 1 || req.getOrderType() > 4) {
            throw new ValidationException("出库类型不正确");
        }
        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库ID不能为空");
        }
        if (req.getStatus() == null || req.getStatus() < 0 || req.getStatus() > 9) {
            throw new ValidationException("状态值不正确");
        }
        if (req.getTenantId() == null) {
            throw new ValidationException("租户ID不能为空");
        }
        if (req.getUserId() == null) {
            throw new ValidationException("用户ID不能为空");
        }

        // 销售出库必须要有客户
        if (req.getOrderType() == 1 && req.getCustomerId() == null) {
            throw new ValidationException("销售出库必须选择客户");
        }

        // 明细校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("出库单明细不能为空");
        }

        // 校验总数量与明细数量一致性
        BigDecimal totalQuantity = req.getItems().stream()
                .map(item -> item.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (req.getTotalQuantity() == null ||
                req.getTotalQuantity().compareTo(totalQuantity) != 0) {
            throw new ValidationException("总数量与明细数量之和不一致");
        }

        // 校验明细数据
        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateReq.ProductInfoInner item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + (i + 1) + "行出库数量必须大于0");
            }

            // 校验批次分配
            validateBatchAllocation(item, i, req.getTenantId());
        }
    }

    /**
     * 构建更新后的出库单主表实体
     */
    private OutboundOrder buildUpdatedOutboundOrder(OutboundCreateReq req, OutboundOrder existingOrder) {
        OutboundOrder order = new OutboundOrder();
        order.setId(req.getId());
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTenantId(req.getTenantId());
        order.setModifiedBy(req.getUserId());
        order.setModifiedAt(new Date());
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));

        // 保留原有创建信息
        order.setCreatedBy(existingOrder.getCreatedBy());
        order.setCreatedAt(existingOrder.getCreatedAt());
        order.setIsDeleted(existingOrder.getIsDeleted());

        return order;
    }

    /**
     * 参数校验
     */
    private void validateCreateReq(OutboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("出库单创建请求不能为空");
        }

        // 基础字段校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        }
        if (req.getOrderType() == null || req.getOrderType() < 1 || req.getOrderType() > 4) {
            throw new ValidationException("出库类型不正确");
        }
        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库ID不能为空");
        }
        if (req.getStatus() == null || req.getStatus() < 0 || req.getStatus() > 9) {
            throw new ValidationException("状态值不正确");
        }
        if (req.getTenantId() == null) {
            throw new ValidationException("租户ID不能为空");
        }
        if (req.getUserId() == null) {
            throw new ValidationException("用户ID不能为空");
        }

        // 销售出库必须要有客户
        if (req.getOrderType() == 1 && req.getCustomerId() == null) {
            throw new ValidationException("销售出库必须选择客户");
        }

        // 明细校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("出库单明细不能为空");
        }

        // 校验总数量与明细数量一致性
        BigDecimal totalQuantity = req.getItems().stream()
                .map(item -> item.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (req.getTotalQuantity() == null ||
                req.getTotalQuantity().compareTo(totalQuantity) != 0) {
            throw new ValidationException("总数量与明细数量之和不一致");
        }

        // 校验明细数据
        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateReq.ProductInfoInner item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + (i + 1) + "行出库数量必须大于0");
            }

            // 校验批次分配
            validateBatchAllocation(item, i, req.getTenantId());
        }
    }

    /**
     * 校验批次分配
     */
    private void validateBatchAllocation(OutboundCreateReq.ProductInfoInner item, int index, Long tenantId) {
        if (item.getBomAllocations() == null || item.getBomAllocations().isEmpty()) {
            throw new ValidationException("第" + (index + 1) + "行产品未分配批次");
        }

        // 计算批次分配总数量
        BigDecimal batchTotalQuantity = item.getBomAllocations().stream()
                .map(batch -> batch.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 校验批次分配数量与出库数量是否一致
        if (batchTotalQuantity.compareTo(item.getQuantity()) != 0) {
            throw new ValidationException("第" + (index + 1) + "行产品批次分配数量(" +
                    batchTotalQuantity + ")与出库数量(" + item.getQuantity() + ")不一致");
        }

        // 校验每个批次的库存是否足够
        for (int i = 0; i < item.getBomAllocations().size(); i++) {
            BomAllocationCreateReq batch = item.getBomAllocations().get(i);

            // 查询批次库存
            InventoryBatch inventoryBatch = inventoryBatchService.selectByBatchNoAndProductId(
                    batch.getBatchNo(), item.getProductId(), tenantId);

            if (inventoryBatch == null) {
                throw new ValidationException("第" + (index + 1) + "行第" + (i + 1) +
                        "个批次不存在: " + batch.getBatchNo());
            }

            if (inventoryBatch.getQuantity().compareTo(batch.getQuantity()) < 0) {
                throw new ValidationException("第" + (index + 1) + "行第" + (i + 1) +
                        "个批次库存不足，可用:" + inventoryBatch.getQuantity() +
                        "，需求:" + batch.getQuantity());
            }
        }
    }

    /**
     * 构建出库单主表实体
     */
    private OutboundOrder buildOutboundOrder(OutboundCreateReq req) {
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTotalAmountUsd(req.getTotalAmountUsd());
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setModifiedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));
        return order;
    }

    /**
     * 构建出库单主表实体
     */
    private OutboundOrder buildOutboundOrder(OutboundCreateSaleProductReq req) {
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setCustomerId(req.getCustomerId());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(req.getTotalQuantity());
        order.setTotalAmount(req.getTotalAmount());
        order.setTotalAmountUsd(req.getTotalAmountUsd());
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setModifiedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);
        order.setExpectedDate(DateUtils.parseDate(req.getExpectedDate()));
        return order;
    }

    /**
     * 构建出库单明细实体列表
     */
    private List<OutboundOrderItem> buildOutboundOrderItems(OutboundCreateReq req, Long orderId) {
        List<OutboundOrderItem> orderItems = new ArrayList<>();

        for (OutboundCreateReq.ProductInfoInner item : req.getItems()) {
            // 为每个批次创建明细记录
            for (BomAllocationCreateReq batch : item.getBomAllocations()) {
                OutboundOrderItem orderItem = new OutboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(batch.getComponentProductId());
                orderItem.setRelationProductId(item.getProductId());
                orderItem.setBatchNo(batch.getBatchNo());
                orderItem.setQuantity(batch.getQuantity()); // 转换为int类型
                orderItem.setShelfLocationId(batch.getShelfId());
                //orderItem.setPriceUnit(item.getPrice());
                //orderItem.setPriceTotal(batch.getQuantity().multiply(item.getPrice()));
                //orderItem.setPriceUnitUsd(item.getPriceUnitUsd());
                //orderItem.setPriceTotalUsd(batch.getQuantity().multiply(item.getPriceUnitUsd()));
                orderItem.setRemark(item.getRemark());
                orderItem.setTenantId(req.getTenantId());
                orderItem.setCreatedBy(req.getUserId());
                orderItem.setModifiedBy(req.getUserId());
                orderItem.setCreatedAt(new Date());
                orderItem.setModifiedAt(new Date());
                orderItem.setIsDeleted(0);

                orderItems.add(orderItem);
            }
        }

        return orderItems;
    }

    private List<OutboundOrderItem> buildOutboundOrderItems(OutboundCreateSaleProductReq req, Long orderId) {
        List<OutboundOrderItem> orderItems = new ArrayList<>();

        for (OutboundCreateSaleProductReq.OrderItemInner item : req.getItems()) {
            // 为每个批次创建明细记录
            for (OutboundCreateSaleProductReq.BatchAllocationInner batch : item.getBatchAllocations()) {
                OutboundOrderItem orderItem = new OutboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(item.getProductId());
                //orderItem.setRelationProductId(item.getProductId());
                orderItem.setBatchNo(batch.getBatchNo());
                orderItem.setQuantity(batch.getQuantity()); // 转换为int类型
                orderItem.setShelfLocationId(batch.getShelfId());
                orderItem.setPriceUnit(item.getPrice());
                orderItem.setPriceTotal(batch.getQuantity().multiply(item.getPrice()));
                orderItem.setPriceUnitUsd(item.getPriceUnitUsd());
                orderItem.setPriceTotalUsd(batch.getQuantity().multiply(item.getPriceUnitUsd()));
                orderItem.setRemark(item.getRemark());
                orderItem.setTenantId(req.getTenantId());
                orderItem.setCreatedBy(req.getUserId());
                orderItem.setModifiedBy(req.getUserId());
                orderItem.setCreatedAt(new Date());
                orderItem.setModifiedAt(new Date());
                orderItem.setIsDeleted(0);

                orderItems.add(orderItem);
            }
        }

        return orderItems;
    }

    /**
     * 更新库存和流水记录
     */
//    private void updateInventoryAndTransaction(OutboundCreateReq req, Long orderId, List<OutboundOrderItem> orderItems) {
//        for (OutboundOrderItem item : orderItems) {
//            // 更新库存
//            updateInventory(req, item);
//
//            // 更新仓库库存
//            updateWarehouseInventory(req, item);
//
//            // 更新批次库存
//            updateInventoryBatch(req, item);
//
//            // 记录库存流水
//            createInventoryTransaction(req, orderId, item);
//        }
//    }

    /**
     * 更新库存
     */
    private void updateInventory(OutboundCreateReq req, OutboundOrderItem item, Long orderId) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), req.getTenantId());

        BigDecimal newQuantity = BigDecimal.ZERO;
        BigDecimal oldQuantity = BigDecimal.ZERO;

        if (existingInventory != null) {
            // 更新现有库存（减少）
            newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());
            oldQuantity = existingInventory.getQuantity();

            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("库存不足，产品ID: " + item.getProductId() +
                        "，当前库存:" + existingInventory.getQuantity() +
                        "，出库数量:" + item.getQuantity());
            }

            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            throw new ValidationException("库存记录不存在，产品ID: " + item.getProductId());
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(req.getWarehouseId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(2); // 2-出库单
        transaction.setOrderId(orderId);
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getQuantity().negate()); // 负数表示减少
        transaction.setBalanceQuantity(newQuantity);
        transaction.setBeforBalanceQuantity(oldQuantity);
        transaction.setTransactionTime(new Date());
        transaction.setTenantId(req.getTenantId());
        transaction.setPriceUnit(item.getPriceUnit());
        transaction.setPriceTotal(item.getPriceTotal());
        //transaction.setRemark(item.getRemark());
        transaction.setCreatedBy(req.getUserId());
        transaction.setModifiedBy(req.getUserId());
        transaction.setCreatedAt(new Date());
        transaction.setModifiedAt(new Date());
        transaction.setIsDeleted(0);

        boolean saved = inventoryTransactionService.save(transaction);
        if (!saved) {
            throw new ValidationException("库存流水记录创建失败");
        }
    }

    /**
     * 更新仓库库存
     */
    private void updateWarehouseInventory(OutboundCreateReq req, OutboundOrderItem item) {
        // 查询现有仓库库存
        InventoryWarehouse existingInventory = inventoryWarehouseService.getByWarehouseAndProduct(
                req.getWarehouseId(), item.getProductId(), req.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存（减少）
            BigDecimal newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());

            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("仓库库存不足，产品ID: " + item.getProductId() +
                        "，当前库存:" + existingInventory.getQuantity() +
                        "，出库数量:" + item.getQuantity());
            }

            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryWarehouseService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("仓库库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            throw new ValidationException("仓库库存记录不存在，产品ID: " + item.getProductId());
        }
    }

    /**
     * 更新批次库存
     */
    private void updateInventoryBatch(OutboundCreateReq req, OutboundOrderItem item) {
        if (!StringUtils.isNotBlank(item.getBatchNo())) {
            return;
        }
        // 查询现有批次库存
        InventoryBatch existingBatch = inventoryBatchService.selectByBatchNoAndProductId(
                item.getBatchNo(), item.getProductId(), req.getTenantId());

        if (existingBatch != null) {
            // 更新现有库存（减少）
            BigDecimal newQuantity = existingBatch.getQuantity().subtract(item.getQuantity());

            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("批次库存不足，批次号: " + item.getBatchNo() +
                        "，产品ID:" + item.getProductId() +
                        "，当前库存:" + existingBatch.getQuantity() +
                        "，出库数量:" + item.getQuantity());
            }

            existingBatch.setQuantity(newQuantity);
            existingBatch.setModifiedBy(req.getUserId());
            existingBatch.setModifiedAt(new Date());

            boolean updated = inventoryBatchService.updateById(existingBatch);
            if (!updated) {
                throw new ValidationException("批次库存更新失败，批次号: " + item.getBatchNo());
            }
        } else {
            throw new ValidationException("批次库存记录不存在，批次号: " + item.getBatchNo());
        }
    }

    /**
     * 创建库存流水记录
     */
    private void createInventoryTransaction(OutboundCreateReq req, Long orderId, OutboundOrderItem item) {

    }

    /**
     * 获取当前库存余额
     */
    private BigDecimal getCurrentBalance(Long productId, Long tenantId) {
        Inventory inventory = inventoryService.getByProduct(productId, tenantId);
        return inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;
    }

    /**
     * 分页查询出库单列表
     */
    public Page<OutboundListPageResp> pageList(Page<OutboundOrder> page, OutboundListPageReq req) {
        Page<OutboundOrder> list = outboundOrderService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> outboundOrderIds = list.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());
        List<OutboundOrderItem> outboundOrderItems = outboundOrderItemService.selectByTenantIdAndOutboundOrderIds(req.getTenantId(), outboundOrderIds);
        Map<Long, List<OutboundOrderItem>> orderId2ItemListMap = outboundOrderItems.stream().collect(Collectors.groupingBy(OutboundOrderItem::getOrderId));

        List<Long> customerIds = list.getRecords().stream().map(v -> v.getCustomerId()).collect(Collectors.toList());
        List<Customer> customerList = customerService.selectByTenantIdAndCustomerIds(req.getTenantId(), customerIds);
        Map<Long, Customer> customerId2CustomerMap = customerList.stream().collect(Collectors.toMap(Customer::getId, v -> v));

        List<Long> warehouseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
        List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
        Map<Long, Warehouse> warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        List<Long> accountIds = list.getRecords().stream().map(v -> v.getCreatedBy()).collect(Collectors.toList());
        List<Account> accounts = accountService.selectByIds(accountIds);
        Map<Long, Account> accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        List<OutboundListPageResp> collect = list.getRecords().stream().map(v -> {
            OutboundListPageResp p = new OutboundListPageResp();
            BeanUtils.copyProperties(v, p);

            p.setCustomerName(customerId2CustomerMap.getOrDefault(v.getCustomerId(), new Customer()).getCustomerName());
            p.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            p.setItemCount(orderId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>()).size());
            p.setApplicantName(accountId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getUsername());
            p.setApplicantAvatar(accountId2AccountMap.getOrDefault(v.getCreatedBy(), new Account()).getAvatarUrl());
            return p;
        }).collect(Collectors.toList());

        Page<OutboundListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    /**
     * 审核通过后更新库存（适用于审核完成后直接出库的场景）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOk(OutboundApproveOkReq approveOkReq) {
        // 1. 查询出库单
        OutboundOrder outboundOrder = outboundOrderService.getById(approveOkReq.getId());
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        // 2. 校验状态
        //if (outboundOrder.getStatus() != 2) { // 2-已通过 TODO yang 加入审核了这里需要改
//            throw new ValidationException("出库单状态不是已审核通过，无法更新库存");
//        }

        // 3. 查询出库单明细
        List<OutboundOrderItem> orderItems = outboundOrderItemService.selectByOrderId(approveOkReq.getId(), approveOkReq.getTenantId());
        if (orderItems.isEmpty()) {
            throw new ValidationException("出库单明细为空");
        }

        // 4. 构建请求对象（用于库存更新）
        OutboundCreateReq req = new OutboundCreateReq();
        req.setWarehouseId(outboundOrder.getWarehouseId());
        req.setTenantId(approveOkReq.getTenantId());
        req.setUserId(approveOkReq.getUserId());

        // 5. 更新库存和流水
        inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems, approveOkReq.getUserId());

        //更新生产任务表
        if (CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode().equals(outboundOrder.getOrderType())) {
            productionTaskService.updateProductionTaskStatus(approveOkReq.getTenantId(), outboundOrder.getId(), CkInOutboundEnums.ProductionTaskStatus.PartialCompletion, approveOkReq.getUserId());
        }

        // 6. 更新出库单状态为已完成
        outboundOrder.setStatus(CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode());
        outboundOrder.setModifiedBy(approveOkReq.getUserId());
        outboundOrder.setModifiedAt(new Date());
        boolean updated = outboundOrderService.updateById(outboundOrder);
        if (!updated) {
            throw new ValidationException("出库单状态更新失败");
        }

        return true;
    }

    /**
     * 修改原有的 updateInventoryAndTransaction 方法，使其可复用
     */
//    private void updateInventoryAndTransaction(OutboundCreateReq req, Long orderId, List<OutboundOrderItem> orderItems) {
//        for (OutboundOrderItem item : orderItems) {
//            // 更新库存
//            updateInventory(req, item, orderId);
//            // 更新仓库库存
//            updateWarehouseInventory(req, item);
//            // 更新批次库存
//            updateInventoryBatch(req, item);
//            // 记录库存流水
//            createInventoryTransaction(req, orderId, item);
//        }
//    }
    public Boolean delete(OutboundDeleteReq req) {
        OutboundOrder p = outboundOrderService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("出库单不存在");
        }

        OutboundOrder save = new OutboundOrder();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        boolean r = outboundOrderService.updateById(save);
        if (!r) {
              throw new ValidationException("出库单删除失败");
        }

        int i = outboundOrderItemService.deleteByOrderId(p.getId(), req.getTenantId(), req.getUserId());
        if (i <= 0) {
            throw new ValidationException("出库单明细删除失败");
        }

        return productionTaskService.delectByOutBoundId(p.getId(), req.getTenantId(), req.getUserId());
    }

    public List<OutboundSaleExcelModel> getOutboundSaleExportData(Long tenantId, Long warehouseId) {
        List<InventoryListResp> result = inventoryFacade.List(warehouseId, tenantId);
        return result.stream().map(v -> {
            OutboundSaleExcelModel model = new OutboundSaleExcelModel();
            model.setProductId(v.getProductId().toString());
            model.setSku(v.getSku());
            model.setName(v.getProductName());
            model.setSpec(v.getSpec());
            model.setColor(v.getColor());
            model.setInventory(v.getAvailableQuantity().toString());
            return model;
        }).collect(Collectors.toList());
    }

    public List<OutBoundSaleQuantityImportResp> importOutboundSaleQuantity(MultipartFile file, Long tenantId, Long userId, Long warehouseId) {
        // 1. 读取Excel数据
        List<OutBoundSaleQuantityImportDto> importDataList = ExcelUtils.readExcel(file, OutBoundSaleQuantityImportDto.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }

        return importDataList.stream().map(v -> {
            OutBoundSaleQuantityImportResp r = new OutBoundSaleQuantityImportResp();
            r.setProductId(Long.valueOf(v.getProductId()));
            r.setSku(v.getSku());
            r.setProductName(v.getProductName());
            r.setQuantity(StringUtils.isBlank(v.getQuantity()) ? BigDecimal.ZERO : new BigDecimal(v.getQuantity()));
            r.setRemark(v.getRemark());
            r.setPrice(StringUtils.isBlank(v.getPrice()) ? null : new BigDecimal(v.getPrice()));
            r.setPriceUnitUsd(StringUtils.isBlank(v.getPriceUsd()) ? null : new BigDecimal(v.getPriceUsd()));
            return r;
        }).collect(Collectors.toList());

    }

    public List<OutboundOderExcelModel> exportOutboundOrderExcel(Long tenantId, Long orderId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (Objects.isNull(outboundOrder)) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> outboundOrderItems = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        List<Long> productIds = outboundOrderItems.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());

        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }


        Map<Long, WarehouseShelf> finalShelfId2ShelfMap = shelfId2ShelfMap;
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        return outboundOrderItems.stream().map(v -> {
            OutboundOderExcelModel model = new OutboundOderExcelModel();
            model.setShelfName(finalShelfId2ShelfMap.getOrDefault(v.getShelfLocationId(), new WarehouseShelf()).getShelfName());
            model.setQuantity(String.valueOf(v.getQuantity()));
            model.setRemark(v.getRemark());
            model.setPriceUnit(v.getPriceUnit().toString());
            model.setPriceTotal(v.getPriceTotal().toString());
            model.setPriceUnitUsd(v.getPriceUnitUsd().toString());
            model.setPriceTotalUsd(v.getPriceTotalUsd().toString());

            if (finalProductId2ProductMap.containsKey(v.getProductId())) {
                Product product = finalProductId2ProductMap.get(v.getProductId());
                model.setEnglishName(product.getEnglishName());
                model.setSku(product.getSku());
                model.setName(product.getName());
                model.setSpec(product.getSpec());
                model.setColor(product.getColor());
                model.setOutUnitHeight(product.getOutUnitHeight() == null || product.getOutUnitHeight().equals(BigDecimal.ZERO) ? "" : product.getOutUnitHeight().toString());
                model.setOutUnitLength(product.getOutUnitLength() == null || product.getOutUnitLength().equals(BigDecimal.ZERO) ? "" : product.getOutUnitLength().toString());
                model.setOutUnitWidth(product.getOutUnitWidth() == null || product.getOutUnitWidth().equals(BigDecimal.ZERO) ? "" : product.getOutUnitWidth().toString());
                model.setWeightPerUnit(product.getWeightPerUnit() == null || product.getWeightPerUnit().equals(BigDecimal.ZERO) ? "" : product.getWeightPerUnit().toString());
                if (product.getOutUnitPerNum() != null) {
                    model.setOutUnitPerNum(product.getOutUnitPerNum() == null ? "" : product.getOutUnitPerNum().toString());

                    // 计算箱数 = 数量 / 出货单位数量 (有小数，则进1)
                    BigDecimal boxNumB = v.getQuantity().divide(product.getOutUnitPerNum(), 2, RoundingMode.HALF_UP);
                    model.setBoxCount(boxNumB.toString());

                    //体积 = 长 * 宽 * 高 * 箱数 / 1000000
                    BigDecimal volumeB = product.getOutUnitHeight()
                            .multiply(product.getOutUnitLength())
                            .multiply(product.getOutUnitWidth())
                            .multiply(boxNumB)
                            .divide(new BigDecimal(1000000), 2, RoundingMode.HALF_UP);
                    model.setVolume(volumeB.toString());


                    //总重量 = 单件重量 * 箱数
                    BigDecimal wall = product.getWeightPerUnit().multiply(boxNumB);
                    model.setWeightAll(wall.toString());
                }
            }
            return model;
        }).collect(Collectors.toList());
    }


    public OutboundDetailResp detail(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }
        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> productId2OutItemListMap = items.stream().collect(Collectors.groupingBy(OutboundOrderItem::getProductId));

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = items.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, Customer> customerId2CustomerMap = new HashMap<>();
        List<Customer> customers = customerService.listEnable(tenantId);
        if (!CollectionUtils.isEmpty(customers)) {
            customerId2CustomerMap = customers.stream().collect(Collectors.toMap(Customer::getId, v -> v));
        }

        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }

        List<OutboundOrderItemSaleExt> outboundOrderItemSaleExts = outboundOrderItemSaleExtService.selectByOrderId(orderId, tenantId);
        Map<Long, OutboundOrderItemSaleExt> productId2OutboundOrderItemSaleExtMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(outboundOrderItemSaleExts)) {
            productId2OutboundOrderItemSaleExtMap = outboundOrderItemSaleExts.stream().collect(Collectors.toMap(OutboundOrderItemSaleExt::getProductId, v -> v));
        }

        OutboundDetailResp resp = new OutboundDetailResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setCustomerId(outboundOrder.getCustomerId());
        resp.setCustomerName(customerId2CustomerMap.getOrDefault(outboundOrder.getCustomerId(), new Customer()).getCustomerName());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        Map<Long, WarehouseShelf> finalShelfId2ShelfMap = shelfId2ShelfMap;

        List<OutboundDetailResp.ProductInfoInner> innerList = new ArrayList<>();

        for (Long productId : productId2OutItemListMap.keySet()) {
            List<OutboundOrderItem> outItemList = productId2OutItemListMap.get(productId);
            if (!CollectionUtils.isEmpty(outItemList)) {
                OutboundDetailResp.ProductInfoInner req = new OutboundDetailResp.ProductInfoInner();
                req.setProductId(productId);
                Product product = finalProductId2ProductMap.getOrDefault(productId, null);
                if (product.getId() != null) {
                    req.setProductName(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getName());
                    req.setSku(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSku());
                    req.setSpec(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSpec());
                    req.setUnit(finalUnitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                }

                //将outItemList 中的getQuantity求和
                BigDecimal quantity = outItemList.stream().map(OutboundOrderItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                req.setQuantity(quantity);
                BigDecimal totalAmount = outItemList.stream().map(OutboundOrderItem::getPriceTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                req.setPriceTotal(totalAmount);
                req.setPriceUnit(outItemList.get(0).getPriceUnit());
                req.setPriceTotalUsd(outItemList.get(0).getPriceTotalUsd());
                req.setPriceUnitUsd(outItemList.get(0).getPriceUnitUsd());
                req.setRemark(outItemList.get(0).getRemark());

                //扩展信息
                if(productId2OutboundOrderItemSaleExtMap.containsKey(productId)) {
                    OutboundOrderItemSaleExt saleExt = productId2OutboundOrderItemSaleExtMap.getOrDefault(productId, new OutboundOrderItemSaleExt());
                    req.setIsTriggerProduct(saleExt.getIsTriggerProduct() == 0 ? true : false);
                    req.setIsRecommend(saleExt.getIsRecommendProduct() == 0 ? true : false);
                    req.setTriggerProductId(saleExt.getTriggerProductId());
                }

                List<OutboundDetailResp.ProductInventoryBatchInner> batchAllocations = outItemList.stream().map(v -> {
                    OutboundDetailResp.ProductInventoryBatchInner i = new OutboundDetailResp.ProductInventoryBatchInner();
                    i.setItemId(v.getId());
                    i.setBatchNo(v.getBatchNo());
                    i.setQuantity(v.getQuantity());
                    i.setShelfId(v.getShelfLocationId());
                    i.setShelfName(finalShelfId2ShelfMap.getOrDefault(v.getShelfLocationId(), new WarehouseShelf()).getShelfName());
                    return i;
                }).collect(Collectors.toList());
                req.setBatchAllocations(batchAllocations);
                innerList.add(req);
            }
        }
        resp.setItems(innerList);
        resp.setItemCount(CollectionUtils.isEmpty(innerList) ? 0 : innerList.size());
        return resp;
    }

    public OutBoundDetailOfProductionResp detailNew(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> parentProductId2OutItemListMap = items.stream()
                .collect(Collectors.groupingBy(OutboundOrderItem::getRelationProductId));

        // 获取产品信息
        Map<Long, Product> productId2ProductMap = new HashMap<>();

        List<Long> allProductIds = Stream.of(
                items.stream().map(OutboundOrderItem::getProductId).distinct().collect(Collectors.toList()),
                items.stream().map(OutboundOrderItem::getRelationProductId).distinct()
                        .collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, allProductIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        // 单位信息
        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        // 仓库信息
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        // 用户信息
        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }

        //货架信息
        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> shelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(shelves)) {
            shelfId2ShelfMap = shelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }

        //生产任务信息
        List<ProductionTask> productionTasks = productionTaskService.selectByOutBoundId(orderId, tenantId);
        Map<Long, ProductionTask> productId2TaskMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(productionTasks)) {
            productId2TaskMap = productionTasks.stream().collect(Collectors.toMap(ProductionTask::getProductId, v -> v));
        }

        // 构建响应对象
        OutBoundDetailOfProductionResp resp = new OutBoundDetailOfProductionResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        resp.setUserId(outboundOrder.getCreatedBy());
        resp.setTenantId(tenantId);

        // 构建产品明细
        List<OutBoundDetailOfProductionResp.ProductionProductItem> productItems = new ArrayList<>();

        for (Long parentProductId : parentProductId2OutItemListMap.keySet()) {
            List<OutboundOrderItem> outItemList = parentProductId2OutItemListMap.get(parentProductId);
            if (!CollectionUtils.isEmpty(outItemList)) {
                OutBoundDetailOfProductionResp.ProductionProductItem productItem = new OutBoundDetailOfProductionResp.ProductionProductItem();

                // 设置产品基本信息
                productItem.setProductId(parentProductId);
                Product product = productId2ProductMap.get(parentProductId);
                if (product != null) {
                    productItem.setProductName(product.getName());
                    productItem.setSku(product.getSku());
                    productItem.setSpec(product.getSpec());
                    productItem.setColor(product.getColor());
                    productItem.setUnit(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                }

                // 计算总数量
//                BigDecimal quantity = outItemList.stream()
//                        .map(OutboundOrderItem::getQuantity)
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//                productItem.setQuantity(quantity);
                productItem.setQuantity(productId2TaskMap.getOrDefault(parentProductId, new ProductionTask()).getMaterialQuantity());

                // 构建BOM组件信息
                List<OutBoundDetailOfProductionResp.BomComponent> bomComponents = buildBomComponents(parentProductId, tenantId, outboundOrder.getWarehouseId(), unitCode2UnitMap, shelfId2ShelfMap);
                productItem.setBomComponents(bomComponents);

                // 构建原料分配信息
                List<OutBoundDetailOfProductionResp.MaterialAllocation> materialAllocations = buildMaterialAllocations(outItemList, shelfId2ShelfMap);
                productItem.setMaterialAllocations(materialAllocations);

                productItems.add(productItem);
            }
        }

        resp.setItems(productItems);
        return resp;
    }

    /**
     * 构建BOM组件信息
     */
    private List<OutBoundDetailOfProductionResp.BomComponent> buildBomComponents(Long productId, Long tenantId, Long warehouseId,
                                                                                 Map<String, Unit> unitCode2UnitMap,
                                                                                 Map<Long, WarehouseShelf> shelfId2ShelfMap) {
        List<OutBoundDetailOfProductionResp.BomComponent> bomComponents = new ArrayList<>();

        try {
            ProductBom productBom = productBomService.selectByProduectId(productId, tenantId);
            // 1. 获取产品的BOM信息
            List<ProductBomDetail> subProductDetails = productBomDetailService.selectByBomId(productBom.getId(), tenantId);
            if (CollectionUtils.isEmpty(subProductDetails)) {
                return bomComponents;
            }

            // 2. 为每个BOM组件构建信息
            Map<Long, List<ProductBomDetail>> componentProductId2ProductBomDetailListMap = subProductDetails.stream()
                    .filter(e-> !CkProductEnums.BomDetailType.BOM_DETAIL_TYPE_PACKAGE.getCode().equals(e.getType()))
                    .collect(Collectors.groupingBy(ProductBomDetail::getComponentProductId));
            List<BomDetailAndWarehouseListNoPackageResp> bomList = new ArrayList<>();

            for (Long componentProductId : componentProductId2ProductBomDetailListMap.keySet()) {
                OutBoundDetailOfProductionResp.BomComponent bomComponent = new OutBoundDetailOfProductionResp.BomComponent();

                List<ProductBomDetail> productBomDetails = componentProductId2ProductBomDetailListMap.getOrDefault(componentProductId, new ArrayList<>());
                // 设置组件基本信息
                bomComponent.setId(productBomDetails.stream().map(ProductBomDetail::getId).min(Comparator.comparingLong(v1 -> v1)).get());
                bomComponent.setComponentProductId(componentProductId);
                bomComponent.setTypeForSort(productBomDetails.stream().map(ProductBomDetail::getType).min(Comparator.comparingInt(v1 -> v1)).get());

                //对 productBomDetails 的quantity求和
                BigDecimal quantityOfSameProduct = BigDecimal.ZERO;
                for (ProductBomDetail productBomDetail : productBomDetails) {
                    quantityOfSameProduct = quantityOfSameProduct.add(productBomDetail.getQuantity());
                }
                bomComponent.setUnitUsage(quantityOfSameProduct); // 单件用量

                // 获取组件产品的详细信息
                Product componentProduct = productService.selectById(tenantId, componentProductId);
                if (componentProduct != null) {
                    bomComponent.setComponentProductName(componentProduct.getName());
                    bomComponent.setComponentProductSku(componentProduct.getSku());
                    bomComponent.setComponentProductSpec(componentProduct.getSpec());

                    // 获取单位信息
                    Unit componentUnit = unitCode2UnitMap.getOrDefault(componentProduct.getUnitCode(), new Unit());
                    bomComponent.setComponentProductUnit(componentUnit != null ? componentUnit.getUnitName() : "");
                }

                // 获取组件的库存批次信息
                List<OutBoundDetailOfProductionResp.StockBatch> availableBatches = getStockBatchesForComponent(
                        componentProductId, warehouseId, tenantId, shelfId2ShelfMap);
                bomComponent.setAvailableBatches(availableBatches);

                List<OutBoundDetailOfProductionResp.UsageDetail> collect = productBomDetails.stream().map(z -> {
                    OutBoundDetailOfProductionResp.UsageDetail usageDetail = new OutBoundDetailOfProductionResp.UsageDetail();
                    usageDetail.setBomDetailId(z.getId());
                    usageDetail.setQuantity(z.getQuantity());
                    usageDetail.setType(z.getType());
                    usageDetail.setLossRate(z.getLossRate());
                    usageDetail.setRemark(z.getRemark());
                    usageDetail.setSortOrder(z.getSortOrder());
                    return usageDetail;
                }).collect(Collectors.toList());
                bomComponent.setUsageDetailList(collect);
                bomComponents.add(bomComponent);
            }
        } catch (Exception e) {
            log.error("构建BOM组件信息失败, productId: {}, tenantId: {}", productId, tenantId, e);
        }
        //bomList 按照 typeForSort 从小到大排序,然后按照id排序
        bomComponents.sort(Comparator.comparingInt(OutBoundDetailOfProductionResp.BomComponent::getTypeForSort).thenComparing(OutBoundDetailOfProductionResp.BomComponent::getId));

        bomComponents.sort(Comparator.comparing(OutBoundDetailOfProductionResp.BomComponent::getComponentProductName));
        return bomComponents;
    }

    /**
     * 获取组件的库存批次信息
     */
    private List<OutBoundDetailOfProductionResp.StockBatch> getStockBatchesForComponent(Long componentProductId, Long warehouseId, Long tenantId, Map<Long, WarehouseShelf> shelfId2ShelfMap) {
        List<OutBoundDetailOfProductionResp.StockBatch> stockBatches = new ArrayList<>();

        try {
            // 调用库存服务获取批次信息
            List<InventoryShelf> inventoryBatches = inventoryShelfService.getBatchShelfStock(tenantId, warehouseId, componentProductId);

            if (CollectionUtils.isEmpty(inventoryBatches)) {
                return stockBatches;
            }

            // 按批次号分组
            Map<String, List<InventoryShelf>> batchNoMap = inventoryBatches.stream()
                    .collect(Collectors.groupingBy(InventoryShelf::getBatchNo));

            for (Map.Entry<String, List<InventoryShelf>> entry : batchNoMap.entrySet()) {
                OutBoundDetailOfProductionResp.StockBatch stockBatch = new OutBoundDetailOfProductionResp.StockBatch();
                stockBatch.setBatchNo(entry.getKey());

                // 计算批次总数量
                BigDecimal totalQuantity = entry.getValue().stream()
                        .map(InventoryShelf::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                stockBatch.setTotalQuantity(totalQuantity);

                // 构建货架信息
                List<OutBoundDetailOfProductionResp.StockShelf> shelves = entry.getValue().stream()
                        .map(inventoryBatch -> {
                            OutBoundDetailOfProductionResp.StockShelf shelf = new OutBoundDetailOfProductionResp.StockShelf();
                            shelf.setShelfId(inventoryBatch.getShelfId());
                            shelf.setShelfName(shelfId2ShelfMap.getOrDefault(inventoryBatch.getShelfId(), new WarehouseShelf()).getShelfName());
                            shelf.setAvailableQuantity(inventoryBatch.getQuantity());
                            return shelf;
                        })
                        .collect(Collectors.toList());
                stockBatch.setShelves(shelves);

                stockBatches.add(stockBatch);
            }
        } catch (Exception e) {
            log.error("获取组件库存批次信息失败, componentProductId: {}, warehouseId: {}", componentProductId, warehouseId, e);
        }

        return stockBatches;
    }

    /**
     * 构建原料分配信息
     */
    private List<OutBoundDetailOfProductionResp.MaterialAllocation> buildMaterialAllocations(List<OutboundOrderItem> outItemList, Map<Long, WarehouseShelf> shelfId2ShelfMap) {
        return outItemList.stream()
                .filter(item -> item.getBatchNo() != null) // 过滤出有批次分配的数据
                .map(item -> {
                    OutBoundDetailOfProductionResp.MaterialAllocation allocation = new OutBoundDetailOfProductionResp.MaterialAllocation();
                    allocation.setComponentProductId(item.getProductId()); // 注意：这里需要根据实际情况调整
                    allocation.setBatchNo(item.getBatchNo());
                    allocation.setShelfId(item.getShelfLocationId());
                    allocation.setShelfName(shelfId2ShelfMap.getOrDefault(item.getShelfLocationId(), new WarehouseShelf()).getShelfName());
                    allocation.setAllocatedQuantity(item.getQuantity());
                    return allocation;
                })
                .collect(Collectors.toList());
    }

    public List<OutBoundComplateProductResp> listCompletedOutBoundProduction(Long tenantId, Integer orderType) {
        if (orderType == null || tenantId == null) {
            return Collections.emptyList();
        }
        List<ProductionTask> productionTasks = productionTaskService.selectRemainingQuantityBT0ByStatus(CkInOutboundEnums.ProductionTaskStatus.PartialCompletion, tenantId);
        if (productionTasks == null || productionTasks.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        List<Long> outBoundIds = productionTasks.stream().map(v -> v.getOutboundOrderId()).distinct().collect(Collectors.toList());
        List<OutboundOrder> outboundOrders = outboundOrderService.selectByOutboundOrderIds(tenantId, outBoundIds);

        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        return outboundOrders.stream().map(outboundOrder -> {
            OutBoundComplateProductResp resp = new OutBoundComplateProductResp();
            resp.setId(outboundOrder.getId());
            resp.setOrderNo(outboundOrder.getOrderNo());
            resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
            resp.setOrderType(outboundOrder.getOrderType());
            resp.setWarehouseId(outboundOrder.getWarehouseId());
            resp.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
            resp.setTotalQuantity(outboundOrder.getTotalQuantity());
            resp.setStatus(outboundOrder.getStatus());
            resp.setRemark(outboundOrder.getRemark());
            return resp;
        }).collect(Collectors.toList());

    }

    public OutBoundDetailOfProductionResp simpleDetailOfProductionOutboundDetail(Long orderId, Long tenantId) {
        OutboundOrder outboundOrder = outboundOrderService.selectById(orderId, tenantId);
        if (outboundOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        List<OutboundOrderItem> items = outboundOrderItemService.selectByOrderId(orderId, tenantId);
        Map<Long, List<OutboundOrderItem>> parentProductId2OutItemListMap = items.stream()
                .collect(Collectors.groupingBy(OutboundOrderItem::getRelationProductId));

        // 获取产品信息
        Map<Long, Product> productId2ProductMap = new HashMap<>();

        List<Long> allProductIds = Stream.of(
                items.stream().map(OutboundOrderItem::getProductId).distinct().collect(Collectors.toList()),
                items.stream().map(OutboundOrderItem::getRelationProductId).distinct()
                        .collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, allProductIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        // 单位信息
        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        // 仓库信息
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        // 用户信息
        Map<Long, Account> accountId2AccountMap = new HashMap<>();
        List<Account> accounts = accountService.selectByIds(Lists.newArrayList(outboundOrder.getCreatedBy(), outboundOrder.getModifiedBy()));
        if (!CollectionUtils.isEmpty(accounts)) {
            accountId2AccountMap = accounts.stream().collect(Collectors.toMap(Account::getId, v -> v));
        }


        //生产任务信息
        List<ProductionTask> productionTasks = productionTaskService.selectByOutBoundId(orderId, tenantId);
        Map<Long, ProductionTask> productId2TaskMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(productionTasks)) {
            productId2TaskMap = productionTasks.stream().collect(Collectors.toMap(ProductionTask::getProductId, v -> v));
        }

        // 构建响应对象
        OutBoundDetailOfProductionResp resp = new OutBoundDetailOfProductionResp();
        resp.setId(outboundOrder.getId());
        resp.setOrderNo(outboundOrder.getOrderNo());
        resp.setOrderType(outboundOrder.getOrderType());
        resp.setRelatedOrderNo(outboundOrder.getRelatedOrderNo());
        resp.setRemark(outboundOrder.getRemark());
        resp.setStatus(outboundOrder.getStatus());
        resp.setTotalQuantity(outboundOrder.getTotalQuantity());
        resp.setTotalAmount(outboundOrder.getTotalAmount());
        resp.setTotalAmountUsd(outboundOrder.getTotalAmountUsd());
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        resp.setUserId(outboundOrder.getCreatedBy());
        resp.setTenantId(tenantId);

        // 构建产品明细
        List<OutBoundDetailOfProductionResp.ProductionProductItem> productItems = new ArrayList<>();

        for (Long parentProductId : parentProductId2OutItemListMap.keySet()) {
            List<OutboundOrderItem> outItemList = parentProductId2OutItemListMap.get(parentProductId);
            if (!CollectionUtils.isEmpty(outItemList)) {
                OutBoundDetailOfProductionResp.ProductionProductItem productItem = new OutBoundDetailOfProductionResp.ProductionProductItem();

                // 设置产品基本信息
                productItem.setProductId(parentProductId);
                Product product = productId2ProductMap.get(parentProductId);
                if (product != null) {
                    productItem.setProductName(product.getName());
                    productItem.setSku(product.getSku());
                    productItem.setSpec(product.getSpec());
                    productItem.setUnit(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                    productItem.setColor(product.getColor());
                }

                productItem.setQuantity(productId2TaskMap.getOrDefault(parentProductId, new ProductionTask()).getRemainingQuantity());
                productItems.add(productItem);
            }
        }

        resp.setItems(productItems);
        return resp;
    }

    /**
     * 校验生产领料单创建/编辑请求参数
     * @param req 请求参数
     * @return 错误信息列表，如果为空表示校验通过
     */
    public void validateOutboundCreateReq(OutboundCreateReq req) {
        // 1. 基础信息校验
        validateBasicInfo(req);

        // 2. 产品明细校验
        validateItems(req.getItems());

        // 3. 数值范围校验
        validateNumericalValues(req);
    }

    /**
     * 基础信息校验
     */
    private void validateBasicInfo(OutboundCreateReq req) {
        // 订单号校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("出库单号不能为空");
        } else if (req.getOrderNo().length() > 100) {
            throw new ValidationException("出库单号长度不能超过100个字符");
        }

        // 订单类型校验
        if (req.getOrderType() == null) {
            throw new ValidationException("出库类型不能为空");
        } else if (req.getOrderType() != CkInOutboundEnums.OutBoundType.ProductionOutbound.getCode()) {
            throw new ValidationException("生产领料单的订单类型必须为2");
        }

        // 仓库ID校验
        if (req.getWarehouseId() == null) {
            throw new ValidationException("请选择出库仓库");
        }

        // 预计出库日期校验
        if (StringUtils.isBlank(req.getExpectedDate())) {
            throw new ValidationException("请选择预计出库日期");
        }

        // 关联单号长度校验
        if (!StringUtils.isBlank(req.getRelatedOrderNo()) && req.getRelatedOrderNo().length() > 100) {
            throw new ValidationException("关联单号长度不能超过100个字符");
        }

        // 备注长度校验
        if (!StringUtils.isBlank(req.getRemark()) && req.getRemark().length() > 200) {
            throw new ValidationException("备注长度不能超过500个字符");
        }
    }

    /**
     * 产品明细校验
     */
    private void validateItems(List<OutboundCreateReq.ProductInfoInner> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new ValidationException("请至少添加一个产品");
        }

        for (int i = 0; i < items.size(); i++) {
            OutboundCreateReq.ProductInfoInner item = items.get(i);
            int itemIndex = i + 1;

            // 产品ID校验
            if (item.getProductId() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的产品ID不能为空");
            }

            // 数量校验
            if (item.getQuantity() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的数量不能为空");
            } else if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + itemIndex + "个产品的数量必须大于0");
            }

            // BOM分配数据校验
            validateBomAllocations(item.getBomAllocations(), itemIndex);
        }
    }

    /**
     * BOM分配数据校验
     */
    private void validateBomAllocations(List<BomAllocationCreateReq> bomAllocations, int itemIndex) {
        if (CollectionUtils.isEmpty(bomAllocations)) {
            // BOM分配可以为空，表示没有分配原料
            return;
        }

        for (int i = 0; i < bomAllocations.size(); i++) {
            BomAllocationCreateReq allocation = bomAllocations.get(i);
            int allocationIndex = i + 1;

            // 组件产品ID校验
            if (Objects.isNull(allocation.getComponentProductId())) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的组件产品ID不能为空");
            }

            // 批次号校验
            if (StringUtils.isBlank(allocation.getBatchNo())) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的批次号不能为空");
            } else if (allocation.getBatchNo().length() > 100) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的批次号长度不能超过100个字符");
            }

            // 货架ID校验
            if (allocation.getShelfId() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的货架ID不能为空");
            }

            // 分配数量校验
            if (allocation.getQuantity() == null) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的分配数量不能为空");
            } else if (allocation.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("第" + itemIndex + "个产品的第" + allocationIndex + "个BOM分配的分配数量必须大于0");
            }
        }
    }

    /**
     * 数值范围校验
     */
    private void validateNumericalValues(OutboundCreateReq req) {
        // 总数量校验
        if (req.getTotalQuantity() != null && req.getTotalQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("总数量不能为负数");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean createProductionPickingOutBound(OutboundCreateReq req) {
        validateOutboundCreateReq(req);
        // 2. 构建出库单主表实体
        OutboundOrder outboundOrder = buildOutboundOrder(req);

        // 3. 插入出库单主表
        boolean orderSaved = outboundOrderService.save(outboundOrder);
        if (!orderSaved) {
            throw new ValidationException("出库单主表保存失败");
        }

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = req.getItems().stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        List<OutboundOrderItem> saveList = new ArrayList<>();
        List<OutboundCreateReq.ProductInfoInner> items = req.getItems();
        for (OutboundCreateReq.ProductInfoInner item : items) {
            List<OutboundOrderItem> orderItems = item.getBomAllocations().stream().map(v -> {
                OutboundOrderItem saveEntity = new OutboundOrderItem();
                saveEntity.setTenantId(req.getTenantId());
                saveEntity.setOrderId(outboundOrder.getId());
                saveEntity.setBatchNo(v.getBatchNo());
                saveEntity.setProductId(v.getComponentProductId());
                saveEntity.setRelationProductId(item.getProductId());
                saveEntity.setBatchNo(v.getBatchNo());
                saveEntity.setShelfLocationId(v.getShelfId());
                saveEntity.setQuantity(v.getQuantity());
                saveEntity.setCreatedAt(new Date());
                saveEntity.setCreatedBy(req.getUserId());
                saveEntity.setModifiedAt(new Date());
                saveEntity.setModifiedBy(req.getUserId());
                return saveEntity;
            }).collect(Collectors.toList());
            saveList.addAll(orderItems);
        }

        // 4. 处理出库单明细
        boolean itemsSaved = outboundOrderItemService.saveBatch(saveList);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        //处理生产任务关联信息
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ProductionTask> batchSaveProductTaskList = items.stream().map(v -> {
            ProductionTask pt = new ProductionTask();
            pt.setTenantId(req.getTenantId());
            pt.setTaskNo("productionTask-" + UUID.randomUUID().toString().substring(0, 8));
            pt.setOutboundOrderId(outboundOrder.getId());
            pt.setOutboundOrderNo(outboundOrder.getOrderNo());
            pt.setProductId(v.getProductId());
            pt.setProductName(finalProductId2ProductMap.getOrDefault(v.getProductId(), new Product()).getName());
            pt.setPlannedQuantity(v.getQuantity());
            pt.setMaterialQuantity(v.getQuantity());
            pt.setProducedQuantity(BigDecimal.ZERO);
            pt.setRemainingQuantity(v.getQuantity());
            pt.setLockQuantity(BigDecimal.ZERO);
            pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
            pt.setWarehouseId(req.getWarehouseId());
            pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
            pt.setRemark(req.getRemark());
            pt.setCreatedAt(new Date());
            pt.setCreatedBy(req.getUserId());
            pt.setModifiedAt(new Date());
            pt.setModifiedBy(req.getUserId());
            return pt;
        }).collect(Collectors.toList());
        boolean saveBatchProductionTaskResult = productionTaskService.saveBatch(batchSaveProductTaskList);
        if (!saveBatchProductionTaskResult) {
            throw new ValidationException("生产任务保存失败");
        }


        // 5. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            inventoryHolder.updateSubInventoryForApprove(outboundOrder, saveList, req.getUserId());
        }
        return true;
    }

    /**
     * 创建销售出库单
     *
     * @param req 出库单创建请求参数
     * @return 创建结果，成功返回true，失败抛出异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createProductionSaleOutBound(OutboundCreateSaleProductReq req) {
        // 1. 校验请求参数的合法性
        validateOutboundRequest(req);

        // 2. 构建出库单主表实体对象
        OutboundOrder outboundOrder = buildOutboundOrder(req);

        // 3. 插入出库单主表数据到数据库
        boolean orderSaved = outboundOrderService.save(outboundOrder);
        if (!orderSaved) {
            throw new ValidationException("出库单主表保存失败");
        }

        // 4. 处理出库单明细
        List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, outboundOrder.getId());
        boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        // 5. 处理销售出库扩展表
        List<OutboundOrderItemSaleExt> outboundOrderItemSaleExts = handleOutboundOrderItemExtensions(req, outboundOrder, orderItems);
        // 批量插入扩展表数据
        if (!CollectionUtils.isEmpty(outboundOrderItemSaleExts)) {
            boolean extSaved = outboundOrderItemSaleExtService.saveBatch(outboundOrderItemSaleExts);
            if (!extSaved) {
                throw new ValidationException("销售出库扩展表保存失败");
            }
        }

        // 6. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems, req.getUserId());
        }
        return true;
    }

    /**
     * 处理销售出库单明细扩展表
     */
    private List<OutboundOrderItemSaleExt> handleOutboundOrderItemExtensions(OutboundCreateSaleProductReq req,
                                                   OutboundOrder outboundOrder,
                                                   List<OutboundOrderItem> orderItems) {
        if (CollectionUtils.isEmpty(req.getItems())) {
            return new ArrayList<>();
        }

        List<OutboundOrderItemSaleExt> extList = new ArrayList<>();

        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateSaleProductReq.OrderItemInner itemReq = req.getItems().get(i);
            OutboundOrderItem orderItem = orderItems.get(i);

            OutboundOrderItemSaleExt ext = new OutboundOrderItemSaleExt();

            // 设置租户ID（假设从请求中获取或从上下文中获取）
            ext.setTenantId(req.getTenantId());

            // 设置出库单ID
            ext.setOrderId(outboundOrder.getId());

            // 设置出库单详情ID
            ext.setOrderItemId(orderItem.getId());

            // 设置产品ID
            ext.setProductId(itemReq.getProductId());

            // 设置是否为触发产品
            // 注意：根据你的表结构注释，0=是，1=不是，所以这里取反
            if (Boolean.TRUE.equals(itemReq.getExtension().getIsTriggerProduct())) {
                ext.setIsTriggerProduct(0); // 0=是触发产品
            } else {
                ext.setIsTriggerProduct(1); // 1=不是触发产品
            }

            // 设置是否为推荐产品
            // 注意：根据你的表结构注释，0=是，1=不是，所以这里取反
            if (Boolean.TRUE.equals(itemReq.getExtension().getIsRecommendProduct())) {
                ext.setIsRecommendProduct(0); // 0=是推荐产品
            } else {
                ext.setIsRecommendProduct(1); // 1=不是推荐产品
            }

            // 设置触发产品ID
            if (itemReq.getExtension().getTriggerProductId() != null && itemReq.getExtension().getTriggerProductId() > 0) {
                ext.setTriggerProductId(itemReq.getExtension().getTriggerProductId());
            } else {
                ext.setTriggerProductId(0L); // 如果没有触发产品，设为0或null，但表结构NOT NULL，所以设为0
            }

            // 设置创建人
            ext.setCreatedBy(req.getUserId());

            // 设置修改人
            ext.setModifiedBy(req.getUserId());

            // 创建时间和修改时间使用数据库默认值

            extList.add(ext);
        }

        return extList;
    }

    /**
     * 校验出库单请求参数
     */
    private void validateOutboundRequest(OutboundCreateSaleProductReq req) {
        // 基本的校验逻辑
        if (req == null) {
            throw new ValidationException("出库单请求参数不能为空");
        }

        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库不能为空");
        }

        if (req.getCustomerId() == null) {
            throw new ValidationException("客户不能为空");
        }

        if (CollectionUtils.isEmpty(req.getItems())) {
            throw new ValidationException("出库产品明细不能为空");
        }

        // 检查产品数量是否有效
        for (OutboundCreateSaleProductReq.OrderItemInner item : req.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("产品出库数量必须大于0");
            }
        }
    }


    /**
     * 校验状态是否允许更新
     */
    private boolean isUpdatableStatus(Integer status) {
        // 根据业务需求定义哪些状态允许更新
        // 例如：草稿状态、待审核状态允许更新，已完成、已取消状态不允许更新
        Integer code = CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode();
        Integer code1 = CkInOutboundEnums.InOutBoundStatus.WaitAudit.getCode();
        List<Integer> canUpdateStatusList = Lists.newArrayList(code, code1);
        return canUpdateStatusList.contains(status);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductionPickingOutBound( OutboundCreateReq req) {
        // 1. 基础信息校验
        validateOutboundCreateReq(req);

        // 1. 校验出库单是否存在且状态可更新
        OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
        if (existingOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        // 校验状态是否允许更新（例如已完成状态的订单可能不允许修改）
        if (!isUpdatableStatus(existingOrder.getStatus())) {
            throw new ValidationException("当前状态的出库单不允许修改");
        }

        // 2. 更新出库单主表
        OutboundOrder outboundOrder = buildOutboundOrder(req);
        outboundOrder.setId(req.getId());
        outboundOrder.setModifiedAt(new Date());
        outboundOrder.setModifiedBy(req.getUserId());

        boolean orderUpdated = outboundOrderService.updateById(outboundOrder);
        if (!orderUpdated) {
            throw new ValidationException("出库单主表更新失败");
        }

        // 3. 删除原有的出库单明细
        LambdaQueryWrapper<OutboundOrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OutboundOrderItem::getOrderId, req.getId());
        boolean itemsDeleted = outboundOrderItemService.remove(itemQueryWrapper);
        if (!itemsDeleted) {
            throw new ValidationException("原有出库单明细删除失败");
        }

        // 4. 重新插入新的出库单明细
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = req.getItems().stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        List<OutboundOrderItem> saveList = new ArrayList<>();
        List<OutboundCreateReq.ProductInfoInner> items = req.getItems();
        for (OutboundCreateReq.ProductInfoInner item : items) {
            List<OutboundOrderItem> orderItems = item.getBomAllocations().stream().map(v -> {
                OutboundOrderItem saveEntity = new OutboundOrderItem();
                saveEntity.setTenantId(req.getTenantId());
                saveEntity.setOrderId(req.getId());
                saveEntity.setBatchNo(v.getBatchNo());
                saveEntity.setProductId(v.getComponentProductId());
                saveEntity.setRelationProductId(item.getProductId());
                saveEntity.setBatchNo(v.getBatchNo());
                saveEntity.setShelfLocationId(v.getShelfId());
                saveEntity.setQuantity(v.getQuantity());
                saveEntity.setCreatedAt(new Date());
                saveEntity.setCreatedBy(req.getUserId());
                saveEntity.setModifiedAt(new Date());
                saveEntity.setModifiedBy(req.getUserId());
                return saveEntity;
            }).collect(Collectors.toList());
            saveList.addAll(orderItems);
        }

        boolean itemsSaved = outboundOrderItemService.saveBatch(saveList);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        // 5. 更新生产任务关联信息
        // 先删除原有的生产任务
        LambdaQueryWrapper<ProductionTask> taskQueryWrapper = new LambdaQueryWrapper<>();
        taskQueryWrapper.eq(ProductionTask::getOutboundOrderId, req.getId());
        boolean tasksDeleted = productionTaskService.remove(taskQueryWrapper);
        if (!tasksDeleted) {
            throw new ValidationException("原有生产任务删除失败");
        }

        // 重新插入新的生产任务
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ProductionTask> batchSaveProductTaskList = items.stream().map(v -> {
            ProductionTask pt = new ProductionTask();
            pt.setTenantId(req.getTenantId());
            pt.setTaskNo("productionTask-" + UUID.randomUUID().toString().substring(0, 8));
            pt.setOutboundOrderId(req.getId());
            pt.setOutboundOrderNo(outboundOrder.getOrderNo());
            pt.setProductId(v.getProductId());
            pt.setProductName(finalProductId2ProductMap.getOrDefault(v.getProductId(), new Product()).getName());
            pt.setPlannedQuantity(v.getQuantity());
            pt.setMaterialQuantity(v.getQuantity());
            pt.setProducedQuantity(BigDecimal.ZERO);
            pt.setRemainingQuantity(v.getQuantity());
            pt.setLockQuantity(BigDecimal.ZERO);
            pt.setStatus(CkInOutboundEnums.ProductionTaskStatus.InProduction.getCode());
            pt.setWarehouseId(req.getWarehouseId());
            pt.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
            pt.setRemark(req.getRemark());
            pt.setCreatedAt(new Date());
            pt.setCreatedBy(req.getUserId());
            pt.setModifiedAt(new Date());
            pt.setModifiedBy(req.getUserId());
            return pt;
        }).collect(Collectors.toList());

        boolean saveBatchProductionTaskResult = productionTaskService.saveBatch(batchSaveProductTaskList);
        if (!saveBatchProductionTaskResult) {
            throw new ValidationException("生产任务保存失败");
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductionSaleOutBound(OutboundCreateSaleProductReq req) {
        // 1. 校验出库单是否存在且状态可更新
        OutboundOrder existingOrder = outboundOrderService.getById(req.getId());
        if (existingOrder == null) {
            throw new ValidationException("出库单不存在");
        }

        // 校验状态是否允许更新
        if (!isUpdatableStatus(existingOrder.getStatus())) {
            throw new ValidationException("当前状态的出库单不允许修改");
        }

        // 2. 更新出库单主表
        OutboundOrder outboundOrder = buildOutboundOrder(req);
        outboundOrder.setId(req.getId());
        outboundOrder.setModifiedAt(new Date());
        outboundOrder.setModifiedBy(req.getUserId());

        boolean orderUpdated = outboundOrderService.updateById(outboundOrder);
        if (!orderUpdated) {
            throw new ValidationException("出库单主表更新失败");
        }

        // 3. 删除原有的出库单明细
        LambdaQueryWrapper<OutboundOrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OutboundOrderItem::getOrderId, req.getId());
        boolean itemsDeleted = outboundOrderItemService.remove(itemQueryWrapper);
        if (!itemsDeleted) {
            throw new ValidationException("原有出库单明细删除失败");
        }

        // 4. 重新插入新的出库单明细
        List<OutboundOrderItem> orderItems = buildOutboundOrderItems(req, req.getId());
        boolean itemsSaved = outboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("出库单明细保存失败");
        }

        return true;
    }

    public OutboundCountOfManagePageResp countsOfManagePage(OutboundListPageReq req) {
        List<OutboundOrder> outboundOrders = outboundOrderService.selectCountsByInboundListPageReq(req);
        if (CollectionUtils.isEmpty(outboundOrders)) {
            return new OutboundCountOfManagePageResp();
        }
        OutboundCountOfManagePageResp r = new OutboundCountOfManagePageResp();
        r.setTotalCount(outboundOrders.size());

        List<OutboundOrder> inboundOrdersOfWaiting = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setWaitApproveCount(inboundOrdersOfWaiting.size());

        List<OutboundOrder> appPassList = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApprovePassCount(appPassList.size());

        List<OutboundOrder> appRejectList = outboundOrders.stream().filter(v ->  CkInOutboundEnums.InOutBoundStatus.Reject.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApproveRejectCount(appRejectList.size());
        return r;
    }
}
