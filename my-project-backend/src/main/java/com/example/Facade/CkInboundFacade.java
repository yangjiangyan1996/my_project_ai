package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.InboundDetailResp;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.enums.CkInOutboundEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
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
    CkUnitService unitService;
    @Resource
    CkProductService productService;

    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkSupplierService supplierService;
    @Resource
    private CkInventoryTransactionService inventoryTransactionService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
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
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(new BigDecimal(req.getTotalQuantity()));
        order.setTenantId(req.getTenantId());
        order.setTotalAmount(req.getTotalAmount());
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
            orderItem.setBatchNo(item.getBatchNo());
            orderItem.setTenantId(req.getTenantId());
            orderItem.setPriceUnit(item.getPriceUnit());
            orderItem.setPriceTotal(item.getPriceTotal());
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

            // 更新仓库库存
            updateWarehouseInventory(req, item);

            // 创建批次库存
            createInventoryBatch(req, item);

            // 记录库存流水
            createInventoryTransaction(req, orderId, item);
        }
    }

    /**
     * 更新库存
     */
    private void updateInventory(InboundCreateReq req, InboundOrderItem item) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), req.getTenantId());

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

    private void updateWarehouseInventory(InboundCreateReq req, InboundOrderItem item) {
        // 查询现有库存
        InventoryWarehouse existingInventory = inventoryWarehouseService.getByWarehouseAndProduct(req.getWarehouseId(), item.getProductId(), req.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryWarehouseService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("仓库库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            InventoryWarehouse newInventory = new InventoryWarehouse();
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

            boolean saved = inventoryWarehouseService.save(newInventory);
            if (!saved) {
                throw new ValidationException("仓库库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }


    private void createInventoryBatch(InboundCreateReq req, InboundOrderItem item) {
        // 查询现有库存
        InventoryBatch ib = inventoryBatchService.selectByBatchNoAndProductId(item.getBatchNo(), item.getProductId(), req.getTenantId());

        if (ib != null) {
            // 更新现有库存
            BigDecimal newQuantity = ib.getQuantity().add(item.getActualQuantity());
            ib.setQuantity(newQuantity);
            ib.setModifiedBy(req.getUserId());
            ib.setModifiedAt(new Date());

            boolean updated = inventoryBatchService.updateById(ib);
            if (!updated) {
                throw new ValidationException("批次库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            InventoryBatch newInventory = new InventoryBatch();
            newInventory.setTenantId(req.getTenantId());
            newInventory.setProductId(item.getProductId());
            newInventory.setBatchNo(item.getBatchNo());
            newInventory.setWarehouseId(req.getWarehouseId());
            newInventory.setQuantity(item.getActualQuantity());
            newInventory.setLockedQuantity(BigDecimal.ZERO);
            newInventory.setInboundOrderId(item.getOrderId());
            newInventory.setInboundItemId(item.getId());
            newInventory.setProductionDate(new Date());
            newInventory.setCreatedBy(req.getUserId());
            newInventory.setModifiedBy(req.getUserId());
            newInventory.setCreatedAt(new Date());
            newInventory.setModifiedAt(new Date());
            newInventory.setIsDeleted(0);

            boolean saved = inventoryBatchService.save(newInventory);
            if (!saved) {
                throw new ValidationException("批次库存创建失败，产品ID: " + item.getProductId());
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
        transaction.setBalanceQuantity(getCurrentBalance(item.getProductId(), req.getTenantId()));
        transaction.setTransactionTime(new Date());
        transaction.setTenantId(req.getTenantId());
        transaction.setPriceUnit(item.getPriceUnit());
        transaction.setPriceTotal(item.getPriceTotal());
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

    public InboundDetailResp detail(Long orderId, Long tenantId) {
        InboundOrder inboundOrder = inboundOrderService.selectById(orderId, tenantId);
        if (inboundOrder == null) {
            throw new ValidationException("入库单不存在");
        }
        List<InboundOrderItem> items = inboundOrderItemService.selectByInboundOrderId(tenantId, orderId);

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


        InboundDetailResp resp = new InboundDetailResp();
        resp.setId(inboundOrder.getId());
        resp.setOrderNo(inboundOrder.getOrderNo());
        resp.setOrderType(inboundOrder.getOrderType());
        resp.setRelatedOrderNo(inboundOrder.getRelatedOrderNo());
        resp.setRemark(inboundOrder.getRemark());
        resp.setStatus(inboundOrder.getStatus());
        resp.setSupplierId(inboundOrder.getSupplierId());
        resp.setTotalQuantity(inboundOrder.getTotalQuantity().doubleValue());
        resp.setWarehouseId(inboundOrder.getWarehouseId());
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        resp.setItems(items.stream().map(item -> {
            InboundDetailResp.InboundDetailCreateReq req = new InboundDetailResp.InboundDetailCreateReq();
            req.setActualQuantity(item.getActualQuantity().doubleValue());
            req.setProductId(item.getProductId());
            req.setBatchNo(item.getBatchNo());
            req.setShelfLocationId(item.getShelfLocationId());
            req.setRemark(item.getRemark());
            Product product = finalProductId2ProductMap.getOrDefault(item.getProductId(), null);
            if (product.getId() != null) {
                req.setSpec(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSpec());
                req.setUnit(finalUnitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
            }
            return req;
        }).collect(Collectors.toList()));
        return resp;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean update(InboundCreateReq req) {
        // 1. 参数校验
        validateUpdateReq(req);

        // 2. 查询现有入库单
        InboundOrder existingOrder = inboundOrderService.getById(req.getId());
        if (existingOrder == null) {
            throw new ValidationException("入库单不存在");
        }

        // 3. 检查状态是否允许编辑
        validateOrderStatusForUpdate(existingOrder.getStatus());

        // 4. 构建更新的入库单主表实体
        InboundOrder inboundOrder = buildUpdateInboundOrder(req, existingOrder);

        // 5. 更新入库单主表
        boolean orderUpdated = inboundOrderService.updateById(inboundOrder);
        if (!orderUpdated) {
            throw new ValidationException("入库单主表更新失败");
        }

        // 6. 处理入库单明细 - 先删除旧的，再插入新的
        boolean itemsDeleted = inboundOrderItemService.deleteByOrderId(req.getTenantId(), req.getId(), req.getUserId());
        if (!itemsDeleted) {
            throw new ValidationException("删除旧明细失败");
        }

        List<InboundOrderItem> orderItems = buildInboundOrderItems(req, req.getId());
        boolean itemsSaved = inboundOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ValidationException("入库单明细保存失败");
        }

        // 7. 如果状态从未完成变为已完成，更新库存和流水
        if (existingOrder.getStatus() != 3 && req.getStatus() == 3) {
            updateInventoryAndTransaction(req, req.getId(), orderItems);
        }

        // 8. 如果状态从已完成变为其他状态，需要回滚库存
        if (existingOrder.getStatus() == 3 && req.getStatus() != 3) {
            rollbackInventory(req, existingOrder);
        }
        return true;
    }

    /**
     * 更新参数校验
     */
    private void validateUpdateReq(InboundCreateReq req) {
        if (req == null) {
            throw new ValidationException("入库单更新请求不能为空");
        }

        // ID校验
        if (req.getId() == null) {
            throw new ValidationException("入库单ID不能为空");
        }

        // 基础字段校验（与创建类似，但有些字段可能不允许修改）
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
     * 检查订单状态是否允许编辑
     */
    private void validateOrderStatusForUpdate(Integer currentStatus) {
        // 只有待提交(0)和已拒绝(4)状态允许编辑
        if (currentStatus != 0 && currentStatus != 4) {
            throw new ValidationException("当前状态不允许编辑，只能编辑待提交或已拒绝的入库单");
        }
    }

    /**
     * 构建更新的入库单主表实体
     */
    private InboundOrder buildUpdateInboundOrder(InboundCreateReq req, InboundOrder existingOrder) {
        InboundOrder order = new InboundOrder();
        order.setId(req.getId());
        order.setOrderNo(req.getOrderNo());
        order.setRelatedOrderNo(req.getRelatedOrderNo());
        order.setOrderType(req.getOrderType());
        order.setWarehouseId(req.getWarehouseId());
        order.setSupplierId(req.getSupplierId());
        order.setRemark(req.getRemark());
        order.setStatus(req.getStatus());
        order.setTotalQuantity(BigDecimal.valueOf(req.getTotalQuantity()));
        order.setModifiedBy(req.getUserId());
        order.setModifiedAt(new Date());

        // 保留原有的创建信息
        order.setTenantId(existingOrder.getTenantId());
        order.setCreatedBy(existingOrder.getCreatedBy());
        order.setCreatedAt(existingOrder.getCreatedAt());
        order.setIsDeleted(existingOrder.getIsDeleted());

        return order;
    }

    /**
     * 回滚库存（当状态从已完成变为其他状态时）
     */
    private void rollbackInventory(InboundCreateReq req, InboundOrder existingOrder) {
        // 查询原有的明细项
        List<InboundOrderItem> oldItems = inboundOrderItemService.selectByInboundOrderId(req.getTenantId(), existingOrder.getId());

        for (InboundOrderItem oldItem : oldItems) {
            // 回滚库存（减去之前入库的数量）
            rollbackSingleInventory(req, oldItem);

            // 可以记录回滚流水，或者标记原有流水为无效
            markTransactionAsInvalid(req, oldItem);
        }
    }

    /**
     * 回滚单个产品的库存
     */
    private void rollbackSingleInventory(InboundCreateReq req, InboundOrderItem oldItem) {
        Inventory existingInventory = inventoryService.getByProduct(oldItem.getProductId(), req.getTenantId());

        if (existingInventory != null) {
            BigDecimal newQuantity = existingInventory.getQuantity().subtract(oldItem.getActualQuantity());
            // 检查库存是否足够回滚
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("库存不足回滚，产品ID: " + oldItem.getProductId());
            }

            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存回滚失败，产品ID: " + oldItem.getProductId());
            }
        }
    }

    /**
     * 标记原有流水为无效
     */
    private void markTransactionAsInvalid(InboundCreateReq req, InboundOrderItem oldItem) {
        // 查询相关的库存流水
        InventoryTransaction transaction = inventoryTransactionService.getByOrderAndProduct(
                req.getTenantId(),
                req.getId(),
                oldItem.getProductId());

        if (transaction != null) {
            transaction.setIsDeleted(1); // 标记为删除
            transaction.setModifiedBy(req.getUserId());
            transaction.setModifiedAt(new Date());

            inventoryTransactionService.updateById(transaction);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOk(InboundApproveOkReq req) {
        // 1. 参数校验
        if (req == null || req.getOrderId() == null) {
            throw new ValidationException("审核请求参数不能为空");
        }

        // 2. 查询入库单
        InboundOrder inboundOrder = inboundOrderService.selectById(req.getOrderId(), req.getTenantId());
        if (inboundOrder == null) {
            throw new ValidationException("入库单不存在");
        }

        // 3. 检查状态是否允许审核通过
        validateOrderStatusForApprove(inboundOrder.getStatus());

        // 4. 查询入库单明细
        List<InboundOrderItem> orderItems = inboundOrderItemService.selectByInboundOrderId(req.getTenantId(), req.getOrderId());
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new ValidationException("入库单明细不能为空");
        }

        // 5. 更新库存和流水记录
        updateInventoryAndTransactionForApprove(inboundOrder, orderItems, req.getUserId());

        // 6. 更新入库单状态为已完成 3-入库已完成
        boolean statusUpdated = updateInboundOrderStatus(inboundOrder.getId(), CkInOutboundEnums.InOutBoundStatus.InOutboundComplete.getCode(), req.getUserId());
        if (!statusUpdated) {
            throw new ValidationException("更新入库单状态失败");
        }

        return true;
    }

    /**
     * 检查订单状态是否允许审核通过
     */
    private void validateOrderStatusForApprove(Integer currentStatus) {
        // TODO yang 等审核流程完成后放开
        //  审核已通过(2)状态允许入库操作
//        if (currentStatus != 2) {
//            throw new ValidationException("当前状态不允许审核通过，只能审核审核中或已通过的入库单");
//        }
    }

    /**
     * 审核通过时更新库存和流水记录
     */
    private void updateInventoryAndTransactionForApprove(InboundOrder inboundOrder, List<InboundOrderItem> orderItems, Long userId) {
        for (InboundOrderItem item : orderItems) {
            // 更新库存
            updateInventoryForApprove(inboundOrder, item, userId);

            // 更新仓库库存
            updateWarehouseInventoryForApprove(inboundOrder, item, userId);

            // 创建库存批次
            updateInventoryBatchApprove(inboundOrder, item, userId);

            // 记录库存流水
            createInventoryTransactionForApprove(inboundOrder, item, userId);
        }
    }

    private void updateInventoryBatchApprove(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
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

    /**
     * 审核通过时更新库存
     */
    private void updateInventoryForApprove(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), inboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            Inventory newInventory = new Inventory();
            newInventory.setProductId(item.getProductId());
            newInventory.setQuantity(item.getActualQuantity());
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
    }

    private void updateWarehouseInventoryForApprove(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        // 查询现有库存
        InventoryWarehouse existingInventory = inventoryWarehouseService.getByWarehouseAndProduct(inboundOrder.getWarehouseId() ,item.getProductId(), inboundOrder.getTenantId());

        if (existingInventory != null) {
            // 更新现有库存
            BigDecimal newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(userId);
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryWarehouseService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
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
                throw new ValidationException("库存创建失败，产品ID: " + item.getProductId());
            }
        }
    }

    /**
     * 审核通过时创建库存流水记录
     */
    private void createInventoryTransactionForApprove(InboundOrder inboundOrder, InboundOrderItem item, Long userId) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(inboundOrder.getWarehouseId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(1); // 1-入库单
        transaction.setOrderId(inboundOrder.getId());
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getActualQuantity()); // 正数表示增加
        transaction.setBalanceQuantity(getCurrentBalanceForApprove(item.getProductId(), inboundOrder.getTenantId()));
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

    /**
     * 获取当前库存余额（审核通过专用）
     */
    private BigDecimal getCurrentBalanceForApprove(Long productId, Long tenantId) {
        Inventory inventory = inventoryService.getByProduct(productId, tenantId);
        return inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;
    }

    /**
     * 更新入库单状态
     */
    private boolean updateInboundOrderStatus(Long orderId, Integer status, Long userId) {
        InboundOrder updateOrder = new InboundOrder();
        updateOrder.setId(orderId);
        updateOrder.setStatus(status);
        updateOrder.setModifiedBy(userId);
        updateOrder.setModifiedAt(new Date());

        return inboundOrderService.updateById(updateOrder);
    }
}
