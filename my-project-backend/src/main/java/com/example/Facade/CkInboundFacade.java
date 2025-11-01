package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.InboundCreateReq;
import com.example.entity.cangku.req.InboundDeleteReq;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.req.InboundUpdateStatusReq;
import com.example.entity.cangku.resp.InboundListPageResp;
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
 * @Date 2025/11/2 00:04
 */
@Service
public class CkInboundFacade {
    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkSupplierService supplierService;
    @Resource
    private CkInventoryTransactionService inventoryTransactionService;
    @Resource
    private CkInventoryService inventoryService;
    @Resource
    private CkInboundOrderItemService inboundOrderItemService;
    @Resource
    private CkInboundOrderService inboundOrderService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean create(InboundCreateReq req) {
        // 1. 参数校验
        validateCreateReq(req);

        // 2. 构建入库单主表实体
        InboundOrder inboundOrder = buildInboundOrder(req);

        // 3. 插入入库单主表
        boolean orderSaved = inboundOrderService.save(inboundOrder);
        if (!orderSaved) {
            throw new ValidationException("入库单主表保存失败");
        }

        // 4. 处理入库单明细
        List<InboundOrderItem> orderItems = buildInboundOrderItems(req, inboundOrder.getId());
        boolean itemsSaved = inboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("入库单明细保存失败");
        }

        // 5. 如果是已完成状态，更新库存和流水
        if (req.getStatus() == 3) { // 已完成状态
            updateInventoryAndTransaction(req, inboundOrder.getId(), orderItems);
        }

        return true;
    }

    /**
     * 参数校验
     */
    private void validateCreateReq(InboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("入库单创建请求不能为空");
        }

        // 基础字段校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw new ValidationException("入库单号不能为空");
        }
        if (req.getOrderType() == null || req.getOrderType() < 1 || req.getOrderType() > 4) {
            throw new ValidationException("入库类型不正确");
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

        // 采购入库必须要有供应商
        if (req.getOrderType() == 1 && req.getSupplierId() == null) {
            throw new ValidationException("采购入库必须选择供应商");
        }

        // 明细校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("入库单明细不能为空");
        }

        // 校验总数量与明细数量一致性
        double totalQuantity = req.getItems().stream()
                .mapToDouble(item -> item.getActualQuantity())
                .sum();

        if (req.getTotalQuantity() == null ||
                Math.abs(req.getTotalQuantity() - totalQuantity) > 0.0001) {
            throw new ValidationException("总数量与明细数量之和不一致");
        }

        // 校验明细数据
        for (int i = 0; i < req.getItems().size(); i++) {
            InboundCreateReq.InboundDetailCreateReq item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getActualQuantity() <= 0) {
                throw new ValidationException("第" + (i + 1) + "行入库数量必须大于0");
            }
        }
    }

    /**
     * 构建入库单主表实体
     */
    private InboundOrder buildInboundOrder(InboundCreateReq req) {
        InboundOrder order = new InboundOrder();
        order.setOrderNo(req.getOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setSupplierId(req.getSupplierId());
        order.setOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(new BigDecimal(req.getTotalQuantity()));
        order.setTenantId(req.getTenantId());
        order.setCreatedBy(req.getUserId());
        order.setModifiedBy(req.getUserId());
        order.setCreatedAt(new Date());
        order.setModifiedAt(new Date());
        order.setIsDeleted(0);

        return order;
    }

    /**
     * 构建入库单明细实体列表
     */
    private List<InboundOrderItem> buildInboundOrderItems(InboundCreateReq req, Long orderId) {
        return req.getItems().stream().map(item -> {
            InboundOrderItem orderItem = new InboundOrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setActualQuantity(new BigDecimal(item.getActualQuantity())); // 入库时计划数量=实际数量
            orderItem.setShelfLocationId(parseShelfLocationId(item.getShelfLocationId()));
            orderItem.setRemark(item.getRemark());
            orderItem.setTenantId(req.getTenantId());
            orderItem.setCreatedBy(req.getUserId());
            orderItem.setModifiedBy(req.getUserId());
            orderItem.setCreatedAt(new Date());
            orderItem.setModifiedAt(new Date());
            orderItem.setIsDeleted(0);

            return orderItem;
        }).collect(Collectors.toList());
    }

    /**
     * 解析货架位置ID
     */
    private Long parseShelfLocationId(String shelfLocationId) {
        if (StringUtils.isBlank(shelfLocationId)) {
            return null;
        }
        try {
            return Long.parseLong(shelfLocationId);
        } catch (NumberFormatException e) {
            throw new ValidationException("货架位置ID格式不正确: " + shelfLocationId);
        }
    }

    /**
     * 更新库存和流水记录
     */
    private void updateInventoryAndTransaction(InboundCreateReq req, Long orderId, List<InboundOrderItem> orderItems) {
        for (InboundOrderItem item : orderItems) {
            // 更新库存
            updateInventory(req, item);

            // 记录库存流水
            createInventoryTransaction(req, orderId, item);
        }
    }

    /**
     * 更新库存
     */
    private void updateInventory(InboundCreateReq req, InboundOrderItem item) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByWarehouseAndProduct(
                req.getWarehouseId(), item.getProductId(), req.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            Inventory newInventory = new Inventory();
            newInventory.setWarehouseId(req.getWarehouseId());
            newInventory.setProductId(item.getProductId());
            newInventory.setQuantity(item.getActualQuantity());
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setTenantId(req.getTenantId());
            newInventory.setCreatedBy(req.getUserId());
            newInventory.setModifiedBy(req.getUserId());
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryService.save(newInventory);
            if (!saved) {
                throw new ValidationException("库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }

    /**
     * 创建库存流水记录
     */
    private void createInventoryTransaction(InboundCreateReq req, Long orderId, InboundOrderItem item) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(req.getWarehouseId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(1); // 1-入库单
        transaction.setOrderId(orderId);
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getActualQuantity()); // 正数表示增加
        transaction.setBalanceQuantity(getCurrentBalance(req.getWarehouseId(), item.getProductId(), req.getTenantId()));
        transaction.setTransactionTime(new Date());
        transaction.setTenantId(req.getTenantId());
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
    private BigDecimal getCurrentBalance(Long warehouseId, Long productId, Long tenantId) {
        Inventory inventory = inventoryService.getByWarehouseAndProduct(warehouseId, productId, tenantId);
        return inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;
    }

    public Page<InboundListPageResp> pageList(Page<InboundOrder> page, InboundListPageReq req) {
        Page<InboundOrder> list = inboundOrderService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> inboundOrderIds = list.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());
        List<InboundOrderItem> inboundOrderItems = inboundOrderItemService.selectByTenantIdAndInboundOrderIds(req.getTenantId(), inboundOrderIds);
        Map<Long, List<InboundOrderItem>> orderId2ItemListMap = inboundOrderItems.stream().collect(Collectors.groupingBy(InboundOrderItem::getOrderId));

        List<Long> supplierIds = list.getRecords().stream().map(v -> v.getSupplierId()).collect(Collectors.toList());
        List<Supplier> supplierList = supplierService.selectByTenantIdAndSupplierIds(req.getTenantId(), supplierIds);
        Map<Long, Supplier> supplierId2SupplierMap = supplierList.stream().collect(Collectors.toMap(Supplier::getId, v -> v));

        List<Long> wareHourseIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
        List<Warehouse> warehouseList = warehouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), wareHourseIds);
        Map<Long, Warehouse> warehouseId2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        List<InboundListPageResp> collect = list.getRecords().stream().map(v -> {
            InboundListPageResp p = new InboundListPageResp();
            BeanUtils.copyProperties(v, p);

            p.setSupplierName(supplierId2SupplierMap.getOrDefault(v.getSupplierId(), new Supplier()).getSupplierName());
            p.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(v.getWarehouseId(), new Warehouse()).getName());
            p.setItemCount(orderId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>()).size());
            return p;
        }).collect(Collectors.toList());

        Page<InboundListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean delete(InboundDeleteReq req) {
        InboundOrder p = inboundOrderService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("入库单不存在");
        }

        InboundOrder save = new InboundOrder();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return inboundOrderService.updateById(save);
    }

    public Boolean updateStatus(InboundUpdateStatusReq req) {
        InboundOrder p = inboundOrderService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("入库单不存在");
        }

        InboundOrder save = new InboundOrder();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return inboundOrderService.updateById(save);
    }
}
