package com.example.Facade;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.InboundCountOfManagePageResp;
import com.example.entity.cangku.resp.InboundDetailResp;
import com.example.entity.cangku.resp.InboundListPageResp;
import com.example.entity.cangku.resp.InboundProductInDetailResp;
import com.example.enums.CkInOutboundEnums;
import com.example.holder.InventoryHolder;
import com.example.holder.ProductTaskHolder;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class CkInboundFacade {
    @Resource
    ProductTaskHolder productTaskHolder;
    @Resource
    CkOutboundOrderService outboundOrderService;
    @Resource
    InventoryHolder inventoryHolder;
    @Resource
    CkUnitService unitService;
    @Resource
    CkProductService productService;
    @Resource
    CkShelfService shelfService;
    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkSupplierService supplierService;
    @Resource
    private CkInventoryTransactionService inventoryTransactionService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    private CkInventoryService inventoryService;
    @Resource
    private CkInboundOrderItemService inboundOrderItemService;
    @Resource
    CkProductionTaskService productionTaskService;
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

        //判断数量生产入库数量是否足够
        //后续要加入审核的时候，要加一个锁定库存
        productTaskHolder.checkProductTaskAndSave(req, orderItems);

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
//        if(StringUtils.isEmpty(req.getExpectedDate())) {
//            throw new ValidationException("入库单的预计入库时间不能为空");
//        }

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
            if (item.getPriceUnit() == null || item.getPriceUnit().compareTo(BigDecimal.ZERO) <= 0) {
//                throw new ValidationException("第" + (i + 1) + "行入库单价必须大于0");
            }
            if (StringUtils.isBlank(item.getBatchNo())) {
                throw new ValidationException("第" + (i + 1) + "行批次号不能为空");
            }
            if (item.getShelfAllocations() == null || item.getShelfAllocations().isEmpty()) {
                throw new ValidationException("第" + (i + 1) + "行货架分配不能为空");
            }
            double sum = item.getShelfAllocations().stream().mapToDouble(v -> v.getQuantity()).sum();
            if (Math.abs(sum - item.getActualQuantity()) > 0.0001) {
                throw new ValidationException("第" + (i + 1) + "行货架分配数量之和与入库数量不一致");
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
        if (req.getExpectedDate() != null) {
            order.setExpectedDate(DateUtil.parseDate(req.getExpectedDate()));
        }
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
        Map<String, OutboundOrder> outboundOrderMap = new HashMap<>();
        Map<String, ProductionTask> outboundNo_productId2InfoMap = new HashMap<>();
        if (req.getOrderType().equals(CkInOutboundEnums.InBoundType.ProductionInbound.getCode())) {
            List<String> outboundOrderNoList = req.getItems().stream().map(v -> v.getRelatedPickingOrderNo()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(outboundOrderNoList)) {
                List<OutboundOrder> outboundOrders = outboundOrderService.selectByOutboundOrderNos(req.getTenantId(), outboundOrderNoList);
                outboundOrderMap = outboundOrders.stream().collect(Collectors.toMap(OutboundOrder::getOrderNo, v -> v));

                List<Long> outboundIds = outboundOrders.stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
                List<ProductionTask> taskList = productionTaskService.selectByOutBoundIds(outboundIds, req.getTenantId());
                outboundNo_productId2InfoMap = taskList.stream().collect(Collectors.toMap(v -> v.getOutboundOrderNo() + "-" + v.getProductId(), v -> v));
            }
        }


        List<InboundOrderItem> orderItems = new ArrayList<>();
        for (InboundCreateReq.InboundDetailCreateReq item : req.getItems()) {
            for (InboundCreateReq.ShelfDetailCreateReq shelf : item.getShelfAllocations()) {
                InboundOrderItem orderItem = new InboundOrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(item.getProductId());
                orderItem.setActualQuantity(new BigDecimal(shelf.getQuantity())); // 入库时计划数量=实际数量
                orderItem.setShelfLocationId(shelf.getShelfLocationId());
                orderItem.setRemark(item.getRemark());
                orderItem.setBatchNo(item.getBatchNo());
                orderItem.setTenantId(req.getTenantId());
                orderItem.setPriceUnit(item.getPriceUnit());
                orderItem.setPriceTotal(item.getPriceTotal());
                orderItem.setProductType(CkInOutboundEnums.ProductType.Product.getCode());
                orderItem.setRelationProductId(outboundOrderMap.getOrDefault(item.getRelatedPickingOrderNo(), new OutboundOrder()).getId());
                orderItem.setRelatedOutboundOrderId(outboundOrderMap.getOrDefault(item.getRelatedPickingOrderNo(), new OutboundOrder()).getId());
                orderItem.setRelatedOutboundOrderNo(item.getRelatedPickingOrderNo());
                orderItem.setProductionTaskId(outboundNo_productId2InfoMap.getOrDefault(item.getRelatedPickingOrderNo() + "-" + item.getProductId(), new ProductionTask()).getId());
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
        log.info("更新库存和流水记录,看看这个方法能被调用到不， 如果调用不到，要删掉");
        for (InboundOrderItem item : orderItems) {
            // 更新库存, 库存流水
            updateInventory(req, item, orderId);

            // 更新仓库库存
            updateWarehouseInventory(req, item);

            // 创建批次库存
            createInventoryBatch(req, item);

            // 记录库存流水
            //createInventoryTransaction(req, orderId, item, oldInventoryQuantity);
        }
    }

    /**
     * 更新库存
     *
     * @return 旧的库存数量
     */
    private BigDecimal updateInventory(InboundCreateReq req, InboundOrderItem item, Long orderId) {
        // 查询现有库存
        Inventory existingInventory = inventoryService.getByProduct(item.getProductId(), req.getTenantId());

        BigDecimal oldQuantity = BigDecimal.ZERO;
        BigDecimal newQuantity = BigDecimal.ZERO;
        if (existingInventory != null) {
            // 更新现有库存
            newQuantity = existingInventory.getQuantity().add(item.getActualQuantity());
            oldQuantity = existingInventory.getQuantity();

            existingInventory.setQuantity(newQuantity);
            existingInventory.setModifiedBy(req.getUserId());
            existingInventory.setModifiedAt(new Date());

            boolean updated = inventoryService.updateById(existingInventory);
            if (!updated) {
                throw new ValidationException("库存更新失败，产品ID: " + item.getProductId());
            }
        } else {
            // 创建新库存记录
            newQuantity = item.getActualQuantity();
            oldQuantity = BigDecimal.ZERO;

            Inventory newInventory = new Inventory();
            newInventory.setProductId(item.getProductId());
            newInventory.setQuantity(newQuantity);
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

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(req.getWarehouseId());
        transaction.setProductId(item.getProductId());
        transaction.setOrderType(1); // 1-入库单
        transaction.setOrderId(orderId);
        transaction.setOrderItemId(item.getId());
        transaction.setChangeQuantity(item.getActualQuantity()); // 正数表示增加
        transaction.setBalanceQuantity(newQuantity);
        transaction.setBeforBalanceQuantity(oldQuantity);
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
        return oldQuantity;
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
            p.setItemCount(orderId2ItemListMap.getOrDefault(v.getId(), new ArrayList<>()).stream().map(s -> s.getProductId()).distinct().collect(Collectors.toList()).size());
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

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, Supplier> supplierId2SupplierMap = new HashMap<>();
        List<Supplier> suppliers = supplierService.listWareHouseEnable(tenantId);
        if (!CollectionUtils.isEmpty(suppliers)) {
            supplierId2SupplierMap = suppliers.stream().collect(Collectors.toMap(Supplier::getId, v -> v));
        }

        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> shelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(shelves)) {
            shelfId2ShelfMap = shelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }


        InboundDetailResp resp = new InboundDetailResp();
        resp.setId(inboundOrder.getId());
        resp.setOrderNo(inboundOrder.getOrderNo());
        resp.setOrderType(inboundOrder.getOrderType());
        resp.setRelatedOrderNo(inboundOrder.getRelatedOrderNo());
        resp.setRemark(inboundOrder.getRemark());
        resp.setStatus(inboundOrder.getStatus());
        resp.setSupplierId(inboundOrder.getSupplierId());
        resp.setTotalQuantity(inboundOrder.getTotalQuantity());
        resp.setTotalAmount(inboundOrder.getTotalAmount());
        resp.setWarehouseId(inboundOrder.getWarehouseId());
        resp.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(inboundOrder.getWarehouseId(), new Warehouse()).getName());
        resp.setSupplierName(supplierId2SupplierMap.getOrDefault(inboundOrder.getSupplierId(), new Supplier()).getSupplierName());
        resp.setCreatedAt(inboundOrder.getCreatedAt());
        resp.setModifiedAt(inboundOrder.getModifiedAt());
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        Map<Long, WarehouseShelf> finalShelfId2ShelfMap = shelfId2ShelfMap;
        Map<Long, List<InboundOrderItem>> productId2InboundItemListMap = items.stream().collect(Collectors.groupingBy(v -> v.getProductId()));

        List<InboundDetailResp.InboundDetailCreateReq> innerList = new ArrayList<>();
        for (Long productId : productId2InboundItemListMap.keySet()) {
            List<InboundOrderItem> inboundOrderItems = productId2InboundItemListMap.get(productId);

            BigDecimal priceTotal = BigDecimal.ZERO;
            BigDecimal actualQuantity = BigDecimal.ZERO;
            List<InboundDetailResp.ShelfDetailCreateReq> shelfAllocations = new ArrayList<>();
            for (InboundOrderItem item : inboundOrderItems) {
                InboundDetailResp.ShelfDetailCreateReq shelfDetail = new InboundDetailResp.ShelfDetailCreateReq();
                shelfDetail.setShelfLocationId(item.getShelfLocationId());
                shelfDetail.setQuantity(item.getActualQuantity());
                shelfDetail.setShelfLocationName(finalShelfId2ShelfMap.getOrDefault(item.getShelfLocationId(), new WarehouseShelf()).getShelfName());
                shelfAllocations.add(shelfDetail);

                priceTotal = priceTotal.add(item.getPriceTotal());
                actualQuantity = actualQuantity.add(item.getActualQuantity());
            }

            InboundDetailResp.InboundDetailCreateReq req = new InboundDetailResp.InboundDetailCreateReq();
            req.setProductId(inboundOrderItems.get(0).getProductId());
            req.setProductName(finalProductId2ProductMap.getOrDefault(inboundOrderItems.get(0).getProductId(), new Product()).getName());
            req.setSku(finalProductId2ProductMap.getOrDefault(inboundOrderItems.get(0).getProductId(), new Product()).getSku());
            req.setBatchNo(inboundOrderItems.get(0).getBatchNo());
            req.setRemark(inboundOrderItems.get(0).getRemark());
            req.setPriceUnit(inboundOrderItems.get(0).getPriceUnit());
            req.setShelfLocationIds(inboundOrderItems.stream().map(v -> v.getShelfLocationId()).distinct().collect(Collectors.toList()));
            req.setPriceTotal(priceTotal);
            req.setActualQuantity(actualQuantity);
            Product product = finalProductId2ProductMap.getOrDefault(inboundOrderItems.get(0).getProductId(), null);
            if (product.getId() != null) {
                req.setSpec(finalProductId2ProductMap.getOrDefault(product.getId(), new Product()).getSpec());
                req.setUnit(finalUnitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
            }
            req.setShelfAllocations(shelfAllocations);

            innerList.add(req);
        }
        resp.setItems(innerList);
        resp.setItemCount(CollectionUtils.isEmpty(resp.getItems()) ? 0 : resp.getItems().size());
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

        //查询 入库明细， 记录ID-数量 ,用于回滚productionTas数据
        List<InboundOrderItem> items = inboundOrderItemService.selectByInboundOrderId(req.getTenantId(), req.getId());
        Map<Long, BigDecimal> productionTaskId2QuantityMap = items.stream().filter(v -> v.getProductionTaskId() != null).collect(Collectors.toMap(v -> v.getProductionTaskId(), v -> v.getActualQuantity()));
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

        //回滚productionTask数量
        if (!productionTaskId2QuantityMap.isEmpty()) {
            List<ProductionTask> productionTasks = productionTaskService.selectByIds(productionTaskId2QuantityMap.keySet(), req.getTenantId());
            productionTasks.forEach(v -> {
                v.setRemainingQuantity(v.getRemainingQuantity().add(productionTaskId2QuantityMap.get(v.getId())));
                v.setLockQuantity(v.getLockQuantity().subtract(productionTaskId2QuantityMap.get(v.getId())));
            });
            boolean productionTasksUpdated = productionTaskService.updateBatchById(productionTasks);
            if (!productionTasksUpdated) {
                throw new ValidationException("回滚生产任务数量失败");
            }
        }

        //判断数量生产入库数量是否足够
        //后续要加入审核的时候，要加一个锁定库存
        productTaskHolder.checkProductTaskAndSave(req, orderItems);

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
        BigDecimal shelfQuantity = BigDecimal.ZERO;
        for (int i = 0; i < req.getItems().size(); i++) {
            InboundCreateReq.InboundDetailCreateReq item = req.getItems().get(i);
            if (item.getProductId() == null) {
                throw new ValidationException("第" + (i + 1) + "行产品ID不能为空");
            }
            if (item.getActualQuantity() <= 0) {
                throw new ValidationException("第" + (i + 1) + "行入库数量必须大于0");
            }
            //求 item中getQuantity的和
            for (InboundCreateReq.ShelfDetailCreateReq s : item.getShelfAllocations()) {
                shelfQuantity = shelfQuantity.add(new BigDecimal(s.getQuantity()));
            }
        }

        if (shelfQuantity.compareTo(BigDecimal.valueOf(req.getTotalQuantity())) != 0) {
            throw new ValidationException("货架分配数量与入库数量不一致");
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
        inventoryHolder.updateAddInventoryForApprove(inboundOrder, orderItems, req.getUserId());

        //更新生产任务
        updateProductionTask(orderItems);

        // 6. 更新入库单状态为已完成 3-入库已完成
        boolean statusUpdated = updateInboundOrderStatus(inboundOrder.getId(), CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode(), req.getUserId());
        if (!statusUpdated) {
            throw new ValidationException("更新入库单状态失败");
        }

        return true;
    }

    private void updateProductionTask(List<InboundOrderItem> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            return;
        }

        // 过滤出有生产任务关联的明细项
        List<InboundOrderItem> itemsWithProductionTask = orderItems.stream()
                .filter(item -> item.getProductionTaskId() != null && item.getProductionTaskId() > 0)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(itemsWithProductionTask)) {
            log.info("入库单明细中没有关联生产任务的数据");
            return;
        }

        // 按生产任务ID分组，汇总入库数量
        Map<Long, InboundOrderItem> taskId2InboundItemMap = itemsWithProductionTask.stream().collect(Collectors.toMap(v -> v.getProductionTaskId(), v -> v));

        // 批量查询生产任务'
        List<Long> productionTaskIds = itemsWithProductionTask.stream().map(v -> v.getProductionTaskId()).distinct().collect(Collectors.toList());
        List<ProductionTask> productionTasks = productionTaskService.listByIds(productionTaskIds);

        // 记录未找到的生产任务（用于日志）
        Set<Long> foundTaskIds = productionTasks.stream()
                .map(ProductionTask::getId)
                .collect(Collectors.toSet());
        Set<Long> notFoundTaskIds = productionTaskIds.stream()
                .filter(id -> !foundTaskIds.contains(id))
                .collect(Collectors.toSet());

        if (!notFoundTaskIds.isEmpty()) {
            log.warn("未找到对应的生产任务数据，任务IDs: {}", notFoundTaskIds);
        }

        if (CollectionUtils.isEmpty(productionTasks)) {
            log.warn("所有生产任务数据都未找到，任务IDs: {}", productionTaskIds);
            return;
        }

        // 构建更新列表
        List<ProductionTask> tasksToUpdate = new ArrayList<>();
        List<String> updateLogs = new ArrayList<>();

        for (ProductionTask task : productionTasks) {
            InboundOrderItem inboundOrderItem = taskId2InboundItemMap.get(task.getId());
            BigDecimal inboundQuantity = inboundOrderItem.getActualQuantity();
            if (inboundQuantity == null || inboundQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 验证锁定库存是否足够
            if (task.getLockQuantity().compareTo(inboundQuantity) < 0) {
                log.warn("生产任务锁定库存不足，任务ID: {}, 任务编号: {}, 锁定数量: {}, 入库数量: {}",
                        task.getId(), task.getTaskNo(), task.getLockQuantity(), inboundQuantity);
                // 这里可以选择跳过或者使用实际锁定数量（根据业务需求）
                // 当前逻辑：跳过该任务更新
                throw new ValidationException("生产任务锁定库存不足");
            }

            // 更新生产任务
            ProductionTask updateTask = new ProductionTask();
            updateTask.setId(task.getId());

            // 减少锁定库存
            BigDecimal newLockQuantity = task.getLockQuantity().subtract(inboundQuantity);
            updateTask.setLockQuantity(newLockQuantity);

            // 更新状态
            Integer newStatus = calculateProductionTaskStatus(newLockQuantity, task.getRemainingQuantity());
            updateTask.setStatus(newStatus);

            tasksToUpdate.add(updateTask);

            // 记录更新日志
            updateLogs.add(String.format("任务[%s]: 入库%.2f, 锁定%.2f→%.2f,  状态→%d",
                    task.getTaskNo(), inboundQuantity,
                    task.getLockQuantity(), newLockQuantity,
                    newStatus));
        }

        // 批量更新生产任务
        if (!CollectionUtils.isEmpty(tasksToUpdate)) {
            boolean updated = productionTaskService.updateBatchById(tasksToUpdate);
            if (!updated) {
                log.error("生产任务批量更新失败，影响记录数: {}", tasksToUpdate.size());
                // 这里不抛异常，避免影响审核主流程
            } else {
                log.info("成功更新 {} 个生产任务: {}", tasksToUpdate.size(), String.join("; ", updateLogs));
            }
        } else {
            log.info("没有需要更新的生产任务");
        }
    }

    /**
     * 计算生产任务状态
     */
    private Integer calculateProductionTaskStatus(BigDecimal lockQuantity, BigDecimal remainingQuantity) {
        if (lockQuantity.compareTo(BigDecimal.ZERO) == 0 && remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return CkInOutboundEnums.ProductionTaskStatus.Completed.getCode();
        } else {
            return CkInOutboundEnums.ProductionTaskStatus.PartialCompletion.getCode();
        }
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

    public InboundProductInDetailResp detailOfProductionInbound(Long orderId, Long tenantId) {
        InboundOrder inboundOrder = inboundOrderService.selectById(orderId, tenantId);
        if (inboundOrder == null) {
            throw new ValidationException("入库单不存在");
        }
        List<InboundOrderItem> inboundItemList = inboundOrderItemService.selectByInboundOrderId(tenantId, orderId);

        //准备数据
        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, Product> productId2ProductMap = new HashMap<>();
        List<Long> productIds = inboundItemList.stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, productIds);
        if (!CollectionUtils.isEmpty(products)) {
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }

        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        Map<String, Unit> unitCode2UnitMap = units.stream()
                .filter(Objects::nonNull)   // 避免空值
                .distinct()                  // 去重
                .collect(Collectors.toMap(Unit::getUnitCode, v -> v));

        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }


        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        Map<Long, WarehouseShelf> finalShelfId2ShelfMap = shelfId2ShelfMap;

        List<InboundProductInDetailResp.InboundItemDetail> items = new ArrayList<>();
        //根据货物ID_关联领料单ID来分组
        Map<String, List<InboundOrderItem>> productId_relationPickingOrderId2InboundItemListMap = inboundItemList.stream().collect(Collectors.groupingBy(v -> v.getProductId() + "_" + v.getRelatedOutboundOrderNo()));
        for (String productId_relationPickingOrderId : productId_relationPickingOrderId2InboundItemListMap.keySet()) {
            List<InboundOrderItem> inboundOrderItems = productId_relationPickingOrderId2InboundItemListMap.get(productId_relationPickingOrderId);
            InboundOrderItem v = inboundOrderItems.get(0);

            InboundProductInDetailResp.InboundItemDetail rd = new InboundProductInDetailResp.InboundItemDetail();
            rd.setProductId(v.getProductId());
            Product product = finalProductId2ProductMap.getOrDefault(v.getProductId(), null);
            if (product != null) {
                rd.setProductName(product.getName());
                rd.setSku(product.getSku());
                rd.setSpec(product.getSpec());
                rd.setUnit(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
            }
            rd.setActualQuantity(v.getActualQuantity());
            rd.setBatchNo(v.getBatchNo());
            rd.setRemark(v.getRemark());
            rd.setRelatedPickingOrderId(v.getRelatedOutboundOrderId());
            rd.setRelatedPickingOrderNo(v.getRelatedOutboundOrderNo());
            rd.setProductionTaskId(v.getProductionTaskId());
            rd.setItemId(v.getId());

            List<Long> shelfIds = inboundOrderItems.stream().map(s -> s.getShelfLocationId()).distinct().collect(Collectors.toList());
            rd.setShelfLocationIds(shelfIds);

            List<InboundProductInDetailResp.ShelfAllocationDetail> shelfAllocations = inboundOrderItems.stream().map(ioi -> {
                InboundProductInDetailResp.ShelfAllocationDetail shelfAllocationDetail = new InboundProductInDetailResp.ShelfAllocationDetail();
                shelfAllocationDetail.setShelfLocationId(ioi.getShelfLocationId());
                shelfAllocationDetail.setShelfLocationName(finalShelfId2ShelfMap.getOrDefault(ioi.getShelfLocationId(), new WarehouseShelf()).getShelfName());
                shelfAllocationDetail.setQuantity(ioi.getActualQuantity());
                shelfAllocationDetail.setShelfCode(finalShelfId2ShelfMap.getOrDefault(ioi.getShelfLocationId(), new WarehouseShelf()).getShelfCode());
                shelfAllocationDetail.setShelfName(finalShelfId2ShelfMap.getOrDefault(ioi.getShelfLocationId(), new WarehouseShelf()).getShelfName());
                return shelfAllocationDetail;
            }).collect(Collectors.toList());
            rd.setShelfAllocations(shelfAllocations);

            items.add(rd);
        }

        InboundProductInDetailResp r = new InboundProductInDetailResp();
        r.setId(inboundOrder.getId());
        r.setOrderNo(inboundOrder.getOrderNo());
        r.setOrderType(inboundOrder.getOrderType());
        r.setRelatedOrderNo(inboundOrder.getRelatedOrderNo());
        r.setRemark(inboundOrder.getRemark());
        r.setStatus(inboundOrder.getStatus());
        r.setWarehouseId(inboundOrder.getWarehouseId());
        r.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(inboundOrder.getWarehouseId(), new Warehouse()).getName());
        r.setExpectedDate(inboundOrder.getExpectedDate());
        r.setCreatedAt(inboundOrder.getCreatedAt());
        r.setModifiedAt(inboundOrder.getModifiedAt());
        r.setItems(items);
        r.setItemCount(items.size());
        r.setTotalQuantity(inboundOrder.getTotalQuantity());
        return r;
    }

    /**
     * 获取数量
     *
     * @param tenantId
     * @return
     */
    public InboundCountOfManagePageResp countsOfManagePage(InboundListPageReq req) {
        List<InboundOrder> inboundOrders = inboundOrderService.selectCountsByInboundListPageReq(req);
        if (CollectionUtils.isEmpty(inboundOrders)) {
            return new InboundCountOfManagePageResp();
        }
        InboundCountOfManagePageResp r = new InboundCountOfManagePageResp();
        r.setTotalCount(inboundOrders.size());

        List<InboundOrder> inboundOrdersOfWaiting = inboundOrders.stream().filter(v -> CkInOutboundEnums.InOutBoundStatus.WaitSubmit.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setWaitApproveCount(inboundOrdersOfWaiting.size());

        List<InboundOrder> appPassList = inboundOrders.stream().filter(v -> CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApprovePassCount(appPassList.size());

        List<InboundOrder> appRejectList = inboundOrders.stream().filter(v -> CkInOutboundEnums.InOutBoundStatus.Reject.getCode().equals(v.getStatus())).collect(Collectors.toList());
        r.setApproveRejectCount(appRejectList.size());
        return r;
    }
}
