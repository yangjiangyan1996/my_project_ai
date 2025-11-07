package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.dto.Account;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    CkInventoryWarehouseService inventoryWarehouseService;

    @Resource
    CkInventoryBatchService inventoryBatchService;

    @Resource
    CkInventoryTransactionService inventoryTransactionService;

    @Resource
    CkCustomerService customerService;

    @Resource
    AccountService accountService;

    @Resource
    CkWareHouseService warehouseService;

    public Boolean create(OutboundCreateReq req) {
        // 1. 参数校验
        validateCreateReq(req);

        // 2. 构建出库单主表实体
        OutboundOrder outboundOrder = buildOutboundOrder(req);

        // 3. 插入出库单主表
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

        // 5. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            updateInventoryAndTransaction(req, outboundOrder.getId(), orderItems);
        }

        return true;
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
        for (int i = 0; i < item.getAvailableBatches().size(); i++) {
            OutboundCreateReq.ProductInventoryBatchInner batch = item.getAvailableBatches().get(i);

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
                orderItem.setPriceUnit(item.getPrice());
                orderItem.setPriceTotal(batch.getQuantity().multiply(item.getPrice()));
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
    private void updateInventory(OutboundCreateReq req, OutboundOrderItem item) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), req.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存（减少）
            BigDecimal newQuantity = existingInventory.getQuantity().subtract(item.getQuantity());

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
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(req.getWarehouseId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(2); // 2-出库单
        transaction.setOrderId(orderId);
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getQuantity().negate()); // 负数表示减少
        transaction.setBalanceQuantity(getCurrentBalance(item.getProductId(), req.getTenantId()));
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
        updateInventoryAndTransaction(req, approveOkReq.getId(), orderItems);

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
    private void updateInventoryAndTransaction(OutboundCreateReq req, Long orderId, List<OutboundOrderItem> orderItems) {
        for (OutboundOrderItem item : orderItems) {
            // 更新库存
            updateInventory(req, item);
            // 更新仓库库存
            updateWarehouseInventory(req, item);
            // 更新批次库存
            updateInventoryBatch(req, item);
            // 记录库存流水
            createInventoryTransaction(req, orderId, item);
        }
    }
}
