package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.req.InventoryTransactionListPageReq;
import com.example.entity.cangku.resp.*;
import com.example.entity.dto.Account;
import com.example.enums.CkCommonEnums;
import com.example.enums.CkInventoryEnums;
import com.example.holder.InventoryHolder;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/3 22:49
 */
@Service
public class CkInventoryFacade {
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkOutboundOrderItemService outboundOrderItemService;
    @Resource
    CkOutboundOrderService outboundOrderService;
    @Resource
    CkUnitService unitService;
    @Resource
    InventoryHolder inventoryHolder;
    @Resource
    CkProductCategoryService productCategoryService;
    @Resource
    CkProductService productService;
    @Resource
    CkWareHouseService wareHouseService;
    @Resource
    CkShelfService shelfService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    AccountService accountService;
    @Resource
    CkInventoryService inventoryService;
    @Resource
    CkInventoryTransactionService inventoryTransactionService;
    @Resource
    private CkInboundOrderService inboundOrderService;
    @Resource
    CkInboundOrderItemService inboundOrderItemService;

    public Page<InventoryPageListResp> pageList(Page<Inventory> page, InventoryListPageReq req) {
        Page<Inventory> list = inventoryService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> productIds = list.getRecords().stream()
                .map(Inventory::getProductId)
                .distinct()
                .collect(Collectors.toList());

        //库存明细
        List<InventoryTransaction> inventoryTransactionList = inventoryTransactionService.selectByProductIds(req.getTenantId(), productIds);

        // 获取相关的仓库信息
        List<Long> wareHouseIds = inventoryTransactionList.stream()
                .map(InventoryTransaction::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());

        List<Warehouse> warehouseList = wareHouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), wareHouseIds);
        Map<Long, Warehouse> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(Warehouse::getId, v -> v));

        // 获取相关的产品信息

        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, v -> v));

        //入库的数据
        List<InboundOrderItem> inboundOrderItems = inboundOrderItemService.selectByProductIds(req.getTenantId(), productIds);
        Map<Long, List<InboundOrderItem>> productId2InboundItemListMap = inboundOrderItems.stream().collect(Collectors.groupingBy(InboundOrderItem::getProductId));
        List<Long> inboundOrderIds = inboundOrderItems.stream()
                .map(InboundOrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        List<InboundOrder> inboundOrders = inboundOrderService.selectByInboundOrderIds(req.getTenantId(), inboundOrderIds);
        Map<Long, InboundOrder> inboundOrderId2InfoMap = inboundOrders.stream()
                .collect(Collectors.toMap(InboundOrder::getId, v -> v));

        //出库的数据
        List<OutboundOrderItem> outboundOrderItems = outboundOrderItemService.selectByProductIds(req.getTenantId(), productIds);
        Map<Long, List<OutboundOrderItem>> productId2OutboundItemListMap = outboundOrderItems.stream().collect(Collectors.groupingBy(OutboundOrderItem::getProductId));
        List<Long> outboundOrderIds = outboundOrderItems.stream()
                .map(OutboundOrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        List<OutboundOrder> outboundOrders = outboundOrderService.selectByInboundOrderIds(req.getTenantId(), outboundOrderIds);
        Map<Long, OutboundOrder> outboundOrderId2InfoMap = outboundOrders.stream()
                .collect(Collectors.toMap(OutboundOrder::getId, v -> v));

        // 获取产品分类信息
        List<ProductCategory> productCategories = productCategoryService.selectByTenantId(req.getTenantId());
        Map<String, ProductCategory> productCode2CategoryMap = productCategories.stream()
                .collect(Collectors.toMap(ProductCategory::getCategoryCode, v -> v));

        // 按产品ID分组库存记录
        Map<Long, List<Inventory>> productInventoryMap = list.getRecords().stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));

        List<Unit> units = unitService.selectByTenantId(req.getTenantId(), 1);
        Map<String, Unit> unitCode2UnitMap = units.stream()
                .filter(Objects::nonNull)   // 避免空值
                .distinct()                  // 去重
                .collect(Collectors.toMap(Unit::getUnitCode, v -> v));

        List<InventoryWarehouse> byProductIds = inventoryWarehouseService.getByProductIds(req.getTenantId(), productIds);
        Map<Long, List<InventoryWarehouse>> productId2InventoryWarehouseListMap = byProductIds.stream()
                .collect(Collectors.groupingBy(InventoryWarehouse::getProductId));


        // 构建响应列表
        List<InventoryPageListResp> resultList = productInventoryMap.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();

                    // 获取产品信息
                    Product product = productMap.getOrDefault(productId, new Product());

                    // 构建响应对象
                    InventoryPageListResp r = new InventoryPageListResp();
                    r.setId(productId);
                    r.setProductId(productId);
                    r.setProductName(product.getName());
                    r.setSpec(product.getSpec());
                    r.setColor(product.getColor());
                    r.setUnitName(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());

                    List<InboundOrderItem> inboundList = productId2InboundItemListMap.getOrDefault(productId, new ArrayList<>());
                    Map<Long, InventoryPageListResp.WarehouseInventory> warehouse2InfoMap = new HashMap<>();
                    for (InboundOrderItem item : inboundList) {
                        Long orderId = item.getOrderId();
                        InboundOrder inboundOrder = inboundOrderId2InfoMap.getOrDefault(orderId, new InboundOrder());
                        if (!inboundOrder.getStatus().equals(3)) {
                            continue;
                        }

                        Long warehouseId = inboundOrder.getWarehouseId();
                        InventoryPageListResp.WarehouseInventory w = null;

                        if (warehouse2InfoMap.containsKey(warehouseId)) {
                            w = warehouse2InfoMap.get(warehouseId);
                            w.setQuantity(w.getQuantity().add(item.getActualQuantity()));
                        } else {
                            w = new InventoryPageListResp.WarehouseInventory();
                            w.setWarehouseId(warehouseId);
                            w.setWarehouseName(warehouseMap.getOrDefault(inboundOrder.getWarehouseId(), new Warehouse()).getName());
                            w.setQuantity(item.getActualQuantity());
                        }
                        warehouse2InfoMap.put(warehouseId, w);
                    }

                    List<InventoryWarehouse> inventoryWareOfProductList
                            = productId2InventoryWarehouseListMap.getOrDefault(productId, new ArrayList<>());
                    //inventoryWareOfProductList 获取map, key是warehouseId ，value是计算每个组中的quantity和，
                    Map<Long,BigDecimal> warehouseId2QuantityMap = inventoryWareOfProductList.stream()
                            .collect(Collectors.groupingBy(InventoryWarehouse::getWarehouseId,
                                    Collectors.mapping(InventoryWarehouse::getQuantity, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
                    List<InventoryPageListResp.WarehouseInventory> warehouseInventoryList = new ArrayList<>();
                    for (Long wareHourseId : warehouseId2QuantityMap.keySet()) {
                        InventoryPageListResp.WarehouseInventory w = new InventoryPageListResp.WarehouseInventory();
                        w.setWarehouseId(wareHourseId);
                        w.setWarehouseName(warehouseMap.getOrDefault(wareHourseId, new Warehouse()).getName());
                        w.setQuantity(warehouseId2QuantityMap.get(wareHourseId));
                        warehouseInventoryList.add(w);
                    }

                    r.setWarehouseInventoryList(warehouseInventoryList);


                    List<OutboundOrderItem> outboundList = productId2OutboundItemListMap.getOrDefault(productId, new ArrayList<>());
                    Map<Long, InventoryPageListResp.WarehouseInventory> outwarehouse2InfoMap = new HashMap<>();
                    for (OutboundOrderItem item : outboundList) {
                        Long orderId = item.getOrderId();
                        OutboundOrder outboundOrder = outboundOrderId2InfoMap.getOrDefault(orderId, new OutboundOrder());
                        if (!outboundOrder.getStatus().equals(3)) {
                            continue;
                        }

                        Long warehouseId = outboundOrder.getWarehouseId();
                        InventoryPageListResp.WarehouseInventory w = new InventoryPageListResp.WarehouseInventory();
                        w.setWarehouseId(warehouseId);
                        w.setWarehouseName(warehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
                        w.setQuantity(item.getQuantity());

                        if (outwarehouse2InfoMap.containsKey(warehouseId)) {
                            w = outwarehouse2InfoMap.get(warehouseId);
                            w.setQuantity(w.getQuantity().add(item.getQuantity()));
                        } else {
                            w = new InventoryPageListResp.WarehouseInventory();
                            w.setWarehouseId(warehouseId);
                            w.setWarehouseName(warehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
                            w.setQuantity(item.getQuantity());
                        }
                        outwarehouse2InfoMap.put(warehouseId, w);
                    }
                    r.setOutboundQuantityList(outwarehouse2InfoMap.values().stream().toList());

                    BigDecimal allCount = entry.getValue().stream()
                            .map(Inventory::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal inboundAllCount = warehouse2InfoMap.values().stream().map(v -> v.getQuantity()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    r.setTotalInQuantityOfAllWarehouses(inboundAllCount);
                    BigDecimal totalOutboundQuantityOfAllWarehouses = outwarehouse2InfoMap.values().stream().map(InventoryPageListResp.WarehouseInventory::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    r.setTotalOutboundQuantityOfAllWarehouses(totalOutboundQuantityOfAllWarehouses);
                    r.setRemainingStockQuantityOfAllWarehouses(allCount);
                    r.setInventoryStatus(CkInventoryEnums.StockStatus.getCodeByCount(allCount, new BigDecimal(product.getMinStock())));
                    r.setOutUnitName(unitCode2UnitMap.getOrDefault(product.getOutUnitCode(), new Unit()).getUnitName());
                    r.setOutUnitPerNum(product.getOutUnitPerNum());

                    //出货单位数量的总数量 = 出货单位数量 / 出货单位数量
                    if (product.getOutUnitPerNum() != null && !Objects.equals(product.getOutUnitPerNum(), BigDecimal.ZERO)) {
                        r.setOutUnitTotalNum(totalOutboundQuantityOfAllWarehouses.divide(product.getOutUnitPerNum(),0, RoundingMode.HALF_UP));
                    }

                    //体积 = 出货单位的长宽高 * 出货单位数量的总数量
                    r.setVolume(product.getOutUnitHeight().multiply(product.getOutUnitLength()).multiply(product.getOutUnitWidth()).multiply(r.getOutUnitTotalNum()).divide(new BigDecimal(1000000),2, RoundingMode.HALF_UP));
                    r.setWeightPerUnit(product.getWeightPerUnit());
                    //总重量 = 单件重量 * 出货单位数量的总数量
                    r.setWeightAll(product.getWeightPerUnit().multiply(totalOutboundQuantityOfAllWarehouses));
                    r.setBarcode(product.getBarcode());
                    r.setSku(product.getSku());

                    //TODO 数据OK了，把这里要替换
                    BigDecimal priceRmb = new BigDecimal(1);
                    r.setPriceRmb(priceRmb);
                    //总价 = 单价 * 库存剩余
                    r.setTotalPriceRmb(priceRmb.multiply(r.getTotalInQuantityOfAllWarehouses()));
                    r.setCategoryName(productCode2CategoryMap.get(product.getCategoryCode()).getCategoryName());
                    return r;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        Page<InventoryPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(resultList);
        return result;
    }


    public List<InventoryListResp> List(Long warehouseId, Long tenantId) {
        List<InventoryWarehouse> inventoryTransactionList = inventoryWarehouseService.selectByWarehourseId(warehouseId, tenantId);
        if (inventoryTransactionList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, InventoryWarehouse> productId2InventoryWareHouseMap = inventoryTransactionList.stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v));

        List<Long> productIds = productId2InventoryWareHouseMap.keySet().stream()
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productService.selectByIds(tenantId, productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, v -> v));

        List<Unit> units = unitService.selectByTenantId(tenantId, CkCommonEnums.Status.Enable.getCode());
        Map<String, Unit> unitCode2UnitMap = units.stream()
                .filter(Objects::nonNull)   // 避免空值
                .distinct()                  // 去重
                .collect(Collectors.toMap(Unit::getUnitCode, v -> v));


        List<InventoryTransaction> inventoryTransactions = inventoryTransactionService.selectByProductIds(tenantId, productIds);
        Map<Long, InventoryTransaction> productId2LatestInventorySactionMap = inventoryHolder.getLatestOutTransactions(inventoryTransactions);


        // 获取产品分类信息
        List<ProductCategory> productCategories = productCategoryService.selectByTenantId(tenantId);
        Map<String, ProductCategory> productCode2CategoryMap = productCategories.stream()
                .collect(Collectors.toMap(ProductCategory::getCategoryCode, v -> v));
        List<InventoryListResp> result = new ArrayList<>();
        for (Long productId : productId2InventoryWareHouseMap.keySet()) {
            InventoryWarehouse inventoryWarehouse = productId2InventoryWareHouseMap.get(productId);

            InventoryListResp r = new InventoryListResp();
            r.setProductId(productId);
            r.setProductName(productMap.get(productId).getName());
            r.setSku(productMap.get(productId).getSku());
            r.setSpec(productMap.get(productId).getSpec());
            r.setColor(productMap.get(productId).getColor());
            r.setUnitName(unitCode2UnitMap.getOrDefault(productMap.getOrDefault(productId, new Product()).getUnitCode(), new Unit()).getUnitName());
            r.setAvailableQuantity(inventoryWarehouse.getQuantity().subtract(inventoryWarehouse.getLockedQuantity()));
            r.setQuantity(inventoryWarehouse.getQuantity());
            r.setLockedQuantity(inventoryWarehouse.getLockedQuantity());
            r.setOutUnitName(unitCode2UnitMap.getOrDefault(productMap.getOrDefault(productId, new Product()).getOutUnitCode(), new Unit()).getUnitName());
            r.setOutUnitPerNum(productMap.get(productId).getOutUnitPerNum());
            r.setCategoryName(productCode2CategoryMap.get(productMap.get(productId).getCategoryCode()).getCategoryName());
            r.setPrice(productId2LatestInventorySactionMap.getOrDefault(productId, new InventoryTransaction()).getPriceUnit());
            result.add(r);
        }

        return result;
    }

    public List<InventoryBatchResp> batches(Long warehouseId, Long productId, Long tenantId) {
        List<InventoryBatch> inventoryBatches = inventoryBatchService.selectByWarehouseIdAndProductId(warehouseId, productId, tenantId);
        if (CollectionUtils.isEmpty(inventoryBatches)) {
            return Collections.emptyList();
        }
        List<InventoryShelf> inventoryShelfList = inventoryShelfService.selectByProductIds(productId, tenantId);

        Map<String, List<InventoryShelf>> batchNo2InventoryShelfMap = inventoryShelfList.stream().collect(Collectors.groupingBy(InventoryShelf::getBatchNo));

        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(tenantId);
        Map<Long, WarehouseShelf> shelfId2InfoMap = warehouseShelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));

        return inventoryBatches.stream().map(c -> {
            InventoryBatchResp resp = new InventoryBatchResp();
            resp.setQuantity(c.getQuantity());
            resp.setBatchNo(c.getBatchNo());
            resp.setShelfList(batchNo2InventoryShelfMap.getOrDefault(c.getBatchNo(), Collections.emptyList()).stream().map(s -> {
                InventoryBatchResp.ShelfInfo shelfInfo = new InventoryBatchResp.ShelfInfo();
                shelfInfo.setShelfId(s.getShelfId());
                shelfInfo.setShelfName(shelfId2InfoMap.getOrDefault(s.getShelfId(), new WarehouseShelf()).getShelfName());
                shelfInfo.setQuantity(s.getQuantity());
                return shelfInfo;
            }).collect(Collectors.toList()));
            return resp;
        }).collect(Collectors.toList());
    }

    public Page<InventoryComprehensiveHistoryResp> comprehensiveHistory(Page<InventoryTransaction> page, InventoryTransactionListPageReq req) {
        // 1. 查询库存流水记录
        Page<InventoryTransaction> transactionList = inventoryTransactionService.getPage(page, req);
        if (transactionList.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<Long> productIds = transactionList.getRecords().stream()
                .map(InventoryTransaction::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 获取仓库信息
        List<Long> warehouseIds = transactionList.getRecords().stream()
                .map(InventoryTransaction::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());

        List<Warehouse> warehouseList = wareHouseService.selectByTenantIdAndWareHouseIds(req.getTenantId(), warehouseIds);
        Map<Long, Warehouse> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(Warehouse::getId, v -> v));

        // 3. 获取产品信息
        List<Product> products = productService.selectByIds(req.getTenantId(), productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, v -> v));

        // 4. 获取单位信息
        List<Unit> units = unitService.selectByTenantId(req.getTenantId(), 1);
        Map<String, Unit> unitCode2UnitMap = units.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(Unit::getUnitCode, v -> v));

        // 5. 获取操作人信息
        List<Long> operatorIds = transactionList.getRecords().stream()
                .map(InventoryTransaction::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<Account> operators = accountService.selectByIds(operatorIds);
        Map<Long, Account> operatorMap = operators.stream()
                .collect(Collectors.toMap(Account::getId, v -> v));

        // 6. 获取总库存数据 (ck_inventory)
        List<Inventory> totalInventoryList = inventoryService.selectByProductIds(req.getTenantId(), productIds);
        Map<Long, Inventory> productTotalInventoryMap = totalInventoryList.stream()
                .collect(Collectors.toMap(Inventory::getProductId, v -> v));

        // 7. 获取仓库库存数据 (ck_inventory_warehouse)
        List<InventoryWarehouse> warehouseInventoryList = inventoryWarehouseService.getByProductIdsAndWarehouseIds(
                req.getTenantId(), productIds, warehouseIds);
        Map<String, InventoryWarehouse> warehouseInventoryMap = warehouseInventoryList.stream()
                .collect(Collectors.toMap(
                        item -> item.getProductId() + "_" + item.getWarehouseId(),
                        v -> v
                ));

        // 8. 获取批次库存数据 (ck_inventory_batch)
        List<InventoryBatch> batchInventoryList = inventoryBatchService.getByProductIdsAndWarehouseIds(
                req.getTenantId(), productIds, warehouseIds);
        Map<String, List<InventoryBatch>> productWarehouseBatchMap = batchInventoryList.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProductId() + "_" + item.getWarehouseId()
                ));

        // 9. 获取单据信息（入库单和出库单）
        List<Long> orderIds = transactionList.getRecords().stream()
                .map(InventoryTransaction::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // 获取入库单信息
        List<InboundOrder> inboundOrders = inboundOrderService.selectByInboundOrderIds(req.getTenantId(),
                orderIds.stream().filter(id -> {
                    // 根据业务类型过滤入库单ID
                    return transactionList.getRecords().stream()
                            .anyMatch(t -> t.getOrderId().equals(id) && t.getOrderType() == 1);
                }).collect(Collectors.toList()));
        Map<Long, InboundOrder> inboundOrderMap = inboundOrders.stream()
                .collect(Collectors.toMap(InboundOrder::getId, v -> v));

        // 获取出库单信息
        List<OutboundOrder> outboundOrders = outboundOrderService.selectByOutboundOrderIds(req.getTenantId(),
                orderIds.stream().filter(id -> {
                    // 根据业务类型过滤出库单ID
                    return transactionList.getRecords().stream()
                            .anyMatch(t -> t.getOrderId().equals(id) && t.getOrderType() == 2);
                }).collect(Collectors.toList()));
        Map<Long, OutboundOrder> outboundOrderMap = outboundOrders.stream()
                .collect(Collectors.toMap(OutboundOrder::getId, v -> v));

        // 10. 获取批次信息
        List<Long> orderItemIds = transactionList.getRecords().stream()
                .map(InventoryTransaction::getOrderItemId)
                .distinct()
                .collect(Collectors.toList());

        // 获取入库批次信息
        List<InboundOrderItem> inboundItems = inboundOrderItemService.selectByOrderItemIds(req.getTenantId(),
                orderItemIds.stream().filter(id -> {
                    return transactionList.getRecords().stream()
                            .anyMatch(t -> t.getOrderItemId().equals(id) && t.getOrderType() == 1);
                }).collect(Collectors.toList()));
        Map<Long, InboundOrderItem> inboundItemMap = inboundItems.stream()
                .collect(Collectors.toMap(InboundOrderItem::getId, v -> v));

        // 获取出库批次信息
        List<OutboundOrderItem> outboundItems = outboundOrderItemService.selectByOrderItemIds(req.getTenantId(),
                orderItemIds.stream().filter(id -> {
                    return transactionList.getRecords().stream()
                            .anyMatch(t -> t.getOrderItemId().equals(id) && t.getOrderType() == 2);
                }).collect(Collectors.toList()));
        Map<Long, OutboundOrderItem> outboundItemMap = outboundItems.stream()
                .collect(Collectors.toMap(OutboundOrderItem::getId, v -> v));

        // 11. 构建响应列表
        List<InventoryComprehensiveHistoryResp> resultList = transactionList.getRecords().stream()
                .map(transaction -> {
                    Long productId = transaction.getProductId();
                    Long warehouseId = transaction.getWarehouseId();
                    String mapKey = productId + "_" + warehouseId;

                    // 获取产品信息
                    Product product = productMap.getOrDefault(productId, new Product());

                    // 构建响应对象
                    InventoryComprehensiveHistoryResp resp = new InventoryComprehensiveHistoryResp();
                    resp.setId(transaction.getId());
                    resp.setTenantId(transaction.getTenantId());
                    resp.setWarehouseId(warehouseId);
                    resp.setWarehouseName(warehouseMap.getOrDefault(warehouseId, new Warehouse()).getName());
                    resp.setProductId(productId);
                    resp.setProductName(product.getName());
                    resp.setColor(product.getColor());
                    resp.setSpec(product.getSpec());
                    resp.setUnitName(unitCode2UnitMap.getOrDefault(product.getUnitCode(), new Unit()).getUnitName());
                    resp.setOrderType(transaction.getOrderType());
                    resp.setOrderId(transaction.getOrderId());
                    resp.setOrderItemId(transaction.getOrderItemId());
                    resp.setChangeQuantity(transaction.getChangeQuantity());
                    resp.setBalanceQuantity(transaction.getBalanceQuantity());
                    resp.setBeforBalanceQuantity(transaction.getBeforBalanceQuantity());
                    resp.setTransactionTime(transaction.getTransactionTime());
                    resp.setPriceUnit(transaction.getPriceUnit());
                    resp.setPriceTotal(transaction.getPriceTotal());

                    // 设置操作人信息
                    Account operator = operatorMap.get(transaction.getCreatedBy());
                    if (operator != null) {
                        resp.setOperatorName(operator.getUsername());
                        resp.setOperatorAvatar(operator.getAvatarUrl());
                    }

                    // 设置单据编号和紧急状态
                    if (transaction.getOrderType() == 1) {
                        InboundOrder inboundOrder = inboundOrderMap.get(transaction.getOrderId());
                        if (inboundOrder != null) {
                            resp.setOrderNo(inboundOrder.getOrderNo());
//                            resp.setIsUrgent(inboundOrder.getIsUrgent());
                        }
                        // 设置批次号（入库单）
                        InboundOrderItem inboundItem = inboundItemMap.get(transaction.getOrderItemId());
                        if (inboundItem != null) {
                            resp.setBatchNo(inboundItem.getBatchNo());
                        }
                    } else if (transaction.getOrderType() == 2) {
                        OutboundOrder outboundOrder = outboundOrderMap.get(transaction.getOrderId());
                        if (outboundOrder != null) {
                            resp.setOrderNo(outboundOrder.getOrderNo());
//                            resp.setIsUrgent(outboundOrder.getIsUrgent());
                        }
                        // 设置批次号（出库单）
                        OutboundOrderItem outboundItem = outboundItemMap.get(transaction.getOrderItemId());
                        if (outboundItem != null) {
                            resp.setBatchNo(outboundItem.getBatchNo());
                        }
                    }

                    // 设置四张表关联数据
                    // 总库存 (ck_inventory)
                    Inventory totalInventory = productTotalInventoryMap.get(productId);
                    resp.setTotalInventory(totalInventory != null ? totalInventory.getQuantity() : BigDecimal.ZERO);

                    // 仓库库存 (ck_inventory_warehouse)
                    InventoryWarehouse warehouseInventory = warehouseInventoryMap.get(mapKey);
                    resp.setWarehouseInventory(warehouseInventory != null ? warehouseInventory.getQuantity() : BigDecimal.ZERO);

                    // 批次库存 (ck_inventory_batch)
                    List<InventoryBatch> batches = productWarehouseBatchMap.get(mapKey);
                    BigDecimal batchTotal = batches != null ?
                            batches.stream()
                                    .map(InventoryBatch::getQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
                    resp.setBatchInventory(batchTotal);

                    // 当前批次数量
                    if (resp.getBatchNo() != null && batches != null) {
                        BigDecimal currentBatchQty = batches.stream()
                                .filter(batch -> resp.getBatchNo().equals(batch.getBatchNo()))
                                .map(InventoryBatch::getQuantity)
                                .findFirst()
                                .orElse(BigDecimal.ZERO);
                        resp.setCurrentBatchQuantity(currentBatchQty);
                    } else {
                        resp.setCurrentBatchQuantity(BigDecimal.ZERO);
                    }

                    return resp;
                })
                .collect(Collectors.toList());

        // 12. 构建分页结果
        Page<InventoryComprehensiveHistoryResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(transactionList.getTotal());
        result.setRecords(resultList);
        return result;
    }

    public InventoryProductDetailResp productInventoryDetail(Long productId, UserInfo user) {
        if (productId == null) {
            return null;
        }
        Inventory inventory = inventoryService.getByProduct(productId, user.getTenantId());

        List<InventoryTransaction> inventoryTransactions = inventoryTransactionService.selectByProductId(user.getTenantId(), productId);
        BigDecimal inCount = BigDecimal.ZERO;
        BigDecimal outCount = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(inventoryTransactions)) {
            inCount = inventoryTransactions.stream().filter(v -> v.getOrderType().equals(1)).map(v -> v.getChangeQuantity().abs()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            outCount = inventoryTransactions.stream().filter(v -> v.getOrderType().equals(2)).map(v -> v.getChangeQuantity().abs()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        //求 inCount 与 outCount 的差值，获取正数
        BigDecimal netChange = inCount.subtract(outCount).abs();

        InventoryProductDetailResp resp = new InventoryProductDetailResp();
        resp.setProductId(productId);
        resp.setCurrentStock(inventory != null ? inventory.getQuantity() : BigDecimal.ZERO);
        resp.setTotalCount(new BigDecimal(inventoryTransactions.size()));
        resp.setInQuantity(inCount);
        resp.setOutQuantity(outCount);
        resp.setNetChange(netChange);
        return resp;
    }

    public List<InventoryBatchResp> lowProductCountChat(Long tenantId) {
//        List<Inventory> inventories = inventoryService.selectByTenantId(tenantId);
//        Map<Long, BigDecimal> inventoryMap = inventories.stream().collect(Collectors.toMap(Inventory::getProductId, Inventory::getQuantity,(k1, k2)->k2));
//
//        List<Product> products = productService.listWareHouseEnable(tenantId);
//        for (Product p : products) {
//            BigDecimal minStock = p.getMinStock() == null ? BigDecimal.ZERO : new BigDecimal(p.getMinStock());
//            BigDecimal inventoryOfProduct = inventoryMap.getOrDefault(p.getId(), BigDecimal.ZERO);
//            if (inventoryOfProduct.compareTo(minStock) < 0) {
//
//            }
//        }
        return null;
    }
}
