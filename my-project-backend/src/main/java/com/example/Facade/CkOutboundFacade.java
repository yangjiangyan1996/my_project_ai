package com.example.Facade;

import com.alibaba.fastjson2.util.DateUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.req.OutboundDeleteReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.req.excel.OutBoundSaleQuantityImportDto;
import com.example.entity.cangku.resp.InventoryListResp;
import com.example.entity.cangku.resp.OutBoundSaleQuantityImportResp;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.cangku.resp.excel.OutboundSaleExcelModel;
import com.example.entity.dto.Account;
import com.example.enums.CkInOutboundEnums;
import com.example.holder.InventoryHolder;
import com.example.service.*;
import com.example.utils.ExcelUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:04
 */
@Service
public class CkOutboundFacade {

    @Resource
    CkOutboundOrderItemService outboundOrderItemService;

    @Resource
    CkOutboundOrderService outboundOrderService;

    @Resource
    CkInventoryService inventoryService;

    @Resource
    InventoryHolder inventoryHolder;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;

    @Resource
    CkInventoryBatchService inventoryBatchService;

    @Resource
    CkInventoryTransactionService inventoryTransactionService;

    @Resource
    CkCustomerService customerService;

    @Resource
    CkProductService productService;
    @Resource
    CkUnitService unitService;
    @Resource
    AccountService accountService;

    @Resource
    CkInventoryFacade inventoryFacade;
    @Resource
    CkWareHouseService warehouseService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean create(OutboundCreateReq req) {

        // 2. 构建出库单主表实体
        OutboundOrder outboundOrder = buildOutboundOrder(req);

        // 3. 插入出库单主表
        boolean orderSaved = outboundOrderService.save(outboundOrder);
        if (!orderSaved) {
            throw new ValidationException("出库单主表保存失败");
        }

        CkInOutboundEnums.OutBoundType out = CkInOutboundEnums.OutBoundType.getByCode(req.getOrderType());
        switch (Objects.requireNonNull(out)) {

            case ProductionOutbound:// 生产领料
                List<OutboundOrderItem> saveList = new ArrayList<>();
                List<OutboundCreateReq.ProductInfoInner> items = req.getItems();
                for (OutboundCreateReq.ProductInfoInner item : items) {
                    List<OutboundOrderItem> orderItems = item.getBomAllocations().stream().map(v -> {
                        OutboundOrderItem saveEntity = new OutboundOrderItem();
                        saveEntity.setTenantId(req.getTenantId());
                        saveEntity.setOrderId(outboundOrder.getId());
                        saveEntity.setProductId(v.getComponentProductId());
                        saveEntity.setRelationProductId(item.getProductId());
                        //item.setBatchNo(outboundOrder.getRelatedOrderNo());
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
                // 5. 如果是已完成状态，更新库存和流水
                if (req.getStatus() == 3) { // 已完成状态
                    //updateInventoryAndTransaction(req, outboundOrder.getId(), orderItems);
                    inventoryHolder.updateSubInventoryForApprove(outboundOrder, saveList, req.getUserId());
                }
                break;

            case SaleOutbound:// 销售出库
                // 1. 参数校验
                validateCreateReq(req);
                // 4. 处理出库单明细
                List<OutboundOrderItem> orderItems1 = buildOutboundOrderItems(req, outboundOrder.getId());
                boolean itemsSaved1 = outboundOrderItemService.saveBatch(orderItems1);
                if (!itemsSaved1) {
                    throw new ValidationException("出库单明细保存失败");
                }
                // 5. 如果是已完成状态，更新库存和流水
                if (req.getStatus() == 3) { // 已完成状态
                   // updateInventoryAndTransaction(req, outboundOrder.getId(), orderItems1);
                    inventoryHolder.updateSubInventoryForApprove(outboundOrder, orderItems1, req.getUserId());
                }
                break;
        }
        return true;
    }

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
        if (item.getBatchAllocations() == null || item.getBatchAllocations().isEmpty()) {
            throw new ValidationException("第" + (index + 1) + "行产品未分配批次");
        }

        // 计算批次分配总数量
        BigDecimal batchTotalQuantity = item.getBatchAllocations().stream()
                .map(batch -> batch.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 校验批次分配数量与出库数量是否一致
        if (batchTotalQuantity.compareTo(item.getQuantity()) != 0) {
            throw new ValidationException("第" + (index + 1) + "行产品批次分配数量(" +
                    batchTotalQuantity + ")与出库数量(" + item.getQuantity() + ")不一致");
        }

        // 校验每个批次的库存是否足够
        for (int i = 0; i < item.getBatchAllocations().size(); i++) {
            OutboundCreateReq.ProductInventoryBatchInner batch = item.getBatchAllocations().get(i);

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
            for (OutboundCreateReq.ProductInventoryBatchInner batch : item.getBatchAllocations()) {
                OutboundOrderItem orderItem = new OutboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(item.getProductId());
                orderItem.setBatchNo(batch.getBatchNo());
                orderItem.setQuantity(batch.getQuantity()); // 转换为int类型
                orderItem.setShelfLocationId(batch.getShelfId());
                orderItem.setPriceUnit(item.getPrice());
                orderItem.setPriceTotal(batch.getQuantity().multiply(item.getPrice()));
                //orderItem.setShelfLocationId(item.gets);
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
        //updateInventoryAndTransaction(req, approveOkReq.getId(), orderItems);

        // 6. 更新出库单状态为已完成
        outboundOrder.setStatus(3); // 3-已完成
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
        resp.setWarehouseId(outboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setExpectedDate(DateUtils.format(outboundOrder.getExpectedDate()));
        resp.setApplicantName(accountId2AccountMap.getOrDefault(outboundOrder.getCreatedBy(), new Account()).getUsername());
        resp.setCreatedAt(outboundOrder.getCreatedAt());
        resp.setUpdatedAt(outboundOrder.getModifiedAt());
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;

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
                req.setRemark(outItemList.get(0).getRemark());

                List<OutboundDetailResp.ProductInventoryBatchInner> batchAllocations = outItemList.stream().map(v -> {
                    OutboundDetailResp.ProductInventoryBatchInner i = new OutboundDetailResp.ProductInventoryBatchInner();
                    i.setItemId(v.getId());
                    i.setBatchNo(v.getBatchNo());
                    i.setQuantity(v.getQuantity());
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
        return outboundOrderService.updateById(save);
    }

    public List<OutboundSaleExcelModel> getOutboundSaleExportData(Long tenantId, Long warehouseId) {
        List<InventoryListResp> result = inventoryFacade.List(warehouseId,tenantId);
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
            r.setQuantity( StringUtils.isBlank(v.getQuantity()) ? BigDecimal.ZERO : new BigDecimal(v.getQuantity()));
            r.setRemark(v.getRemark());
            r.setPrice(StringUtils.isBlank(v.getPrice()) ? null :new BigDecimal(v.getPrice()));
            return r;
        }).collect(Collectors.toList());

    }
}
