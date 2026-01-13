package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.InventoryAlertReq;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.req.InventoryTransactionListPageReq;
import com.example.entity.cangku.req.OutBoundBatchAllocationCheckRequest;
import com.example.entity.cangku.resp.*;
import com.example.entity.dto.Account;
import com.example.enums.CkCommonEnums;
import com.example.enums.CkInOutboundEnums;
import com.example.enums.CkInventoryEnums;
import com.example.holder.InventoryHolder;
import com.example.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
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
    CkShelfZoneService shelfService;
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
    CkProductBomService productBomService;
    @Resource
    CkProductBomDetailService productBomDetailService;
    @Resource
    CkInboundOrderItemService inboundOrderItemService;

    public Page<InventoryPageListResp> pageList(Page<Inventory> page, InventoryListPageReq req) {
        List<Product> p = new ArrayList<>();
        if (StringUtils.isNotBlank(req.getProductName())) {
            if (CollectionUtils.isEmpty(p)) {
                p = productService.selectByTenantId(req.getTenantId());
            }
            if (!CollectionUtils.isEmpty(p)) {
                List<Long> productIdsOfName = p.stream().filter(v -> v.getName().contains(req.getProductName())).map(v -> v.getId()).collect(Collectors.toList());
                req.setProductIdsOfName(productIdsOfName);
            }
        }
        if (StringUtils.isNotBlank(req.getSku())) {
            if (CollectionUtils.isEmpty(p)) {
                p = productService.selectByTenantId(req.getTenantId());
            }
            if (!CollectionUtils.isEmpty(p)) {
                List<Long> productIdsOfSku = p.stream().filter(v -> v.getSku().contains(req.getSku())).map(v -> v.getId()).collect(Collectors.toList());
                req.setProductIdsOfSku(productIdsOfSku);
            }
        }
        if (req.getCategoryId() != null) {
            if (CollectionUtils.isEmpty(p)) {
                p = productService.selectByTenantId(req.getTenantId());
            }
            if (!CollectionUtils.isEmpty(p)) {
                ProductCategory category = productCategoryService.selectById(req.getTenantId(), req.getCategoryId());
                List<Long> productIdsOfCategory = p.stream().filter(v -> v.getCategoryCode().equals(category.getCategoryCode())).map(v -> v.getId()).collect(Collectors.toList());
                req.setProductIdsOfCategoryId(productIdsOfCategory);
            }
        }

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

        List<ShelfZone> warehouseShelves = shelfService.selectByTenantId(req.getTenantId());
        Map<Long, ShelfZone> shelfId2ShelfMap = warehouseShelves.stream()
                .collect(Collectors.toMap(ShelfZone::getId, v -> v));

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

        List<InventoryShelf> inventoryShelves = inventoryShelfService.selectByProductIds(productIds, req.getTenantId());
        Map<Long, List<InventoryShelf>> productId2InventoryShelfListMap = inventoryShelves.stream()
                .collect(Collectors.groupingBy(InventoryShelf::getProductId));


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
                        if (inboundOrder.getStatus() < CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode()) {
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

                    List<InventoryShelf> inventoryShelfOfProductList =
                            productId2InventoryShelfListMap.getOrDefault(productId, new ArrayList<>());

                    //inventoryWareOfProductList 获取map, key是warehouseId ，value是计算每个组中的quantity和，
                    Map<Long, BigDecimal> warehouseId2QuantityMap = inventoryWareOfProductList.stream()
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

                    Map<Long, BigDecimal> shelfId2QuantityMap = inventoryShelfOfProductList.stream()
                            .collect(Collectors.groupingBy(InventoryShelf::getShelfId,
                                    Collectors.mapping(InventoryShelf::getQuantity, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

                    List<InventoryPageListResp.ShelfInventory> shelfInventoryList = new ArrayList<>();
                    for (Long shelfId : shelfId2QuantityMap.keySet()) {
                        InventoryPageListResp.ShelfInventory w = new InventoryPageListResp.ShelfInventory();
                        w.setShelfId(shelfId);
                        w.setShelfName(shelfId2ShelfMap.getOrDefault(shelfId, new ShelfZone()).getShelfName());
                        w.setQuantity(shelfId2QuantityMap.get(shelfId));
                        shelfInventoryList.add(w);
                    }
                    r.setShelfInventoryList(shelfInventoryList);


                    List<OutboundOrderItem> outboundList = productId2OutboundItemListMap.getOrDefault(productId, new ArrayList<>());
                    Map<Long, InventoryPageListResp.WarehouseInventory> outwarehouse2InfoMap = new HashMap<>();
                    for (OutboundOrderItem item : outboundList) {
                        Long orderId = item.getOrderId();
                        OutboundOrder outboundOrder = outboundOrderId2InfoMap.getOrDefault(orderId, new OutboundOrder());
                        if (CkInOutboundEnums.InOutBoundStatus.AuditPass.getCode().equals(outboundOrder.getStatus()) || CkInOutboundEnums.InOutBoundStatus.InOutboundComplete.getCode().equals(outboundOrder.getStatus())) {

                        } else {
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
                        r.setOutUnitTotalNum(totalOutboundQuantityOfAllWarehouses.divide(product.getOutUnitPerNum(), 0, RoundingMode.HALF_UP));
                    }

                    //体积 = 出货单位的长宽高 * 出货单位数量的总数量
                    if (product.getOutUnitHeight() != null
                            && product.getOutUnitLength() != null
                            && product.getOutUnitWidth() != null
                            && r.getOutUnitTotalNum() != null
                            && !Objects.equals(product.getOutUnitHeight(), BigDecimal.ZERO)
                            && !Objects.equals(product.getOutUnitLength(), BigDecimal.ZERO)
                            && !Objects.equals(product.getOutUnitWidth(), BigDecimal.ZERO)
                            && !Objects.equals(r.getOutUnitTotalNum(), BigDecimal.ZERO)) {
                        r.setVolume(product.getOutUnitHeight().multiply(product.getOutUnitLength()).multiply(product.getOutUnitWidth()).multiply(r.getOutUnitTotalNum()).divide(new BigDecimal(1000000), 2, RoundingMode.HALF_UP));
                    }
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


    public List<InventoryListResp> List(Long warehouseId, Long tenantId, List<Long> productIdsOfChoose) {
        List<InventoryWarehouse> inventoryTransactionList = inventoryWarehouseService.selectByWarehourseId(warehouseId, productIdsOfChoose, tenantId);
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
            r.setPriceUnitUsd(productId2LatestInventorySactionMap.getOrDefault(productId, new InventoryTransaction()).getPriceUnitUsd());
            result.add(r);
        }

        return result;
    }

    public List<InventoryBatchResp> batches(Long warehouseId, Long productId, Long tenantId) {
        List<InventoryBatch> inventoryBatches = inventoryBatchService.selectByWarehouseIdAndProductId(warehouseId, productId, tenantId);
        if (CollectionUtils.isEmpty(inventoryBatches)) {
            return Collections.emptyList();
        }
        List<InventoryShelf> inventoryShelfList = inventoryShelfService.selectByProductId(productId, tenantId);

        Map<String, List<InventoryShelf>> batchNo2InventoryShelfMap = inventoryShelfList.stream().collect(Collectors.groupingBy(InventoryShelf::getBatchNo));

        List<ShelfZone> warehouseShelves = shelfService.selectByTenantId(tenantId);
        Map<Long, ShelfZone> shelfId2InfoMap = warehouseShelves.stream().collect(Collectors.toMap(ShelfZone::getId, v -> v));

        return inventoryBatches.stream().map(c -> {
            InventoryBatchResp resp = new InventoryBatchResp();
            resp.setQuantity(c.getQuantity());
            resp.setBatchNo(c.getBatchNo());
            resp.setCreatedAt(c.getCreatedAt());
            resp.setShelfList(batchNo2InventoryShelfMap.getOrDefault(c.getBatchNo(), Collections.emptyList()).stream().map(s -> {
                InventoryBatchResp.ShelfInfo shelfInfo = new InventoryBatchResp.ShelfInfo();
                shelfInfo.setShelfId(s.getShelfId());
                shelfInfo.setShelfName(shelfId2InfoMap.getOrDefault(s.getShelfId(), new ShelfZone()).getShelfName());
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
                    resp.setOrderTypeDetail(transaction.getOrderTypeDetail());
                    resp.setOrderId(transaction.getOrderId());
                    resp.setOrderItemId(transaction.getOrderItemId());
                    resp.setChangeQuantity(transaction.getChangeQuantity());
                    resp.setBalanceQuantity(transaction.getBalanceQuantity());
                    resp.setBeforBalanceQuantity(transaction.getBeforBalanceQuantity());
                    resp.setTransactionTime(transaction.getTransactionTime());
                    resp.setPriceUnit(transaction.getPriceUnit());
                    resp.setPriceTotal(transaction.getPriceTotal());
                    resp.setPriceUnitUsd(transaction.getPriceUnitUsd());
                    resp.setPriceTotalUsd(transaction.getPriceTotalUsd());

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

//    public OutBoundBatchAllocationCheckResponse checkBatchAllocation(OutBoundBatchAllocationCheckRequest request) {
//        OutBoundBatchAllocationCheckResponse response = new OutBoundBatchAllocationCheckResponse();
//        List<OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO> availableList = new ArrayList<>();
//
//        // 1. 获取所有涉及的原料商品ID
//        Set<Long> componentProductIds = request.getProductAllocations().stream()
//                .flatMap(item -> item.getBomAllocations().stream())
//                .map(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getComponentProductId)
//                .collect(Collectors.toSet());
//        if (componentProductIds.isEmpty()) {
//            List<Long> productIds = request.getProductAllocations().stream().map(v -> v.getProductId()).distinct().collect(Collectors.toList());
//            List<ProductBom> productBoms = productBomService.selectByProduectIds(productIds, request.getTenantId());
//
//            List<Long> bomIds = productBoms.stream().map(ProductBom::getId).distinct().collect(Collectors.toList());
//
//            List<ProductBomDetail> bomDetailList = productBomDetailService.selectByBomIds(bomIds, request.getTenantId());
//            componentProductIds = bomDetailList.stream().map(ProductBomDetail::getComponentProductId).collect(Collectors.toSet());
//        }
//
//        // 查询数据库获取实际库存
//        List<InventoryShelf> inventoryList = inventoryShelfService.getBatchShelfStock(request.getTenantId(), request.getWarehouseId(), componentProductIds);
//
//        // 2. 获取产品批次货架的实际库存
//        Map<String, BigDecimal> productBatchShelf2CountMap = inventoryList.stream().collect(Collectors.toMap(
//                stock -> buildBatchShelfKey(stock.getProductId(), stock.getBatchNo(), stock.getShelfId()),
//                InventoryShelf::getQuantity
//        ));
//
//        // 3. 获取产品批次的实际库存
//        Map<String, BigDecimal> productBatch2CountMap = inventoryList.stream()
//                .filter(inventory -> inventory.getProductId() != null && inventory.getBatchNo() != null)
//                .collect(Collectors.toMap(
//                        inventory -> buildBatchShelfKey(inventory.getProductId(), inventory.getBatchNo(), -1L),
//                        inventory -> inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO,
//                        BigDecimal::add
//                ));
//
//        // 3. 计算每个批次货架的剩余数量
//        calculateTotalAllocation(request, request.getCurrentProductId(),  productBatchShelf2CountMap, productBatch2CountMap);
//
//        // 4. 检查每个分配是否超出库存
//        boolean hasInsufficient = false;
//        StringBuilder errorMessage = new StringBuilder();
//
//        for (OutBoundBatchAllocationCheckRequest.ProductAllocationDTO productAlloc : request.getProductAllocations()) {
//            if(request.getCurrentProductId() != null && !request.getCurrentProductId().equals(productAlloc.getProductId())) {
//                // 其他商品，分配数据不变， 货架和总批次库存数量实时变化
//                for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc : productAlloc.getBomAllocations()) {
//                    OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO availableDTO = new OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO();
//                    availableDTO.setComponentProductId(bomAlloc.getComponentProductId());
//                    availableDTO.setBatchNo(bomAlloc.getBatchNo());
//                    availableDTO.setShelfId(bomAlloc.getShelfId());
//
//                    String key = buildBatchShelfKey(bomAlloc.getComponentProductId(), bomAlloc.getBatchNo(), bomAlloc.getShelfId());
//                    String keyOfBatch = buildBatchShelfKey(bomAlloc.getComponentProductId(), bomAlloc.getBatchNo(), -1L);
//
//                    BigDecimal remainCount = productBatchShelf2CountMap.getOrDefault(key, BigDecimal.ZERO);
//                    BigDecimal remainOfBatchCount = productBatch2CountMap.getOrDefault(keyOfBatch, BigDecimal.ZERO);
//
//                    availableDTO.setAvailableQuantity(remainCount);
//                    availableDTO.setAllocatedQuantity(bomAlloc.getQuantity());
//                    availableDTO.setBatchAvailableQuantity(remainOfBatchCount);
//                    availableList.add(availableDTO);
//                }
//            } else {
//                for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc : productAlloc.getBomAllocations()) {
//                    String key = buildBatchShelfKey(bomAlloc.getComponentProductId(), bomAlloc.getBatchNo(), bomAlloc.getShelfId());
//                    String keyOfBatch = buildBatchShelfKey(bomAlloc.getComponentProductId(), bomAlloc.getBatchNo(), -1L);
//
//                    BigDecimal remainCount = productBatchShelf2CountMap.getOrDefault(key, BigDecimal.ZERO);
//                    BigDecimal remainOfBatchCount = productBatch2CountMap.getOrDefault(keyOfBatch, BigDecimal.ZERO);
//
//
//                    //已分配数量
//                    BigDecimal allocatedQuantity = BigDecimal.ZERO;
//                    //剩余数量
//                    //BigDecimal remainingQuantity = BigDecimal.ZERO;
//                    //货架可用数量
//                    BigDecimal shelfAvailableQuantity = BigDecimal.ZERO;
//                    ///批次可用数量
//                    BigDecimal batchAvailableQuantity = BigDecimal.ZERO;
//
//                    //如果剩余数量 > bomAlloc的quantity, 则表示可以分配
//                    if (remainCount.compareTo(bomAlloc.getQuantity()) >= 0) {
//                        allocatedQuantity = bomAlloc.getQuantity();
//                        shelfAvailableQuantity = remainCount.subtract(allocatedQuantity);
//                        batchAvailableQuantity = remainOfBatchCount.subtract(allocatedQuantity);
//                    } else {
//                        if (remainCount.compareTo(BigDecimal.ZERO) > 0) {
//                            allocatedQuantity = remainCount;
//                            shelfAvailableQuantity = BigDecimal.ZERO;
//                            batchAvailableQuantity = remainOfBatchCount.subtract(remainCount);
//                            hasInsufficient = true;
//                            errorMessage.append(String.format("原料%s批次%s货架%s: 分配%s > 剩余%s; ", bomAlloc.getComponentProductName(), bomAlloc.getBatchNo(), bomAlloc.getShelfName(),
//                                    bomAlloc.getQuantity(), shelfAvailableQuantity));
//                        } else {
//                            allocatedQuantity = BigDecimal.ZERO;
//                            shelfAvailableQuantity = BigDecimal.ZERO;
//                            batchAvailableQuantity = remainOfBatchCount;
//                            hasInsufficient = true;
//                            errorMessage.append(String.format("原料%s批次%s货架%s: 分配%s > 剩余%s; ", bomAlloc.getComponentProductName(), bomAlloc.getBatchNo(), bomAlloc.getShelfName(),
//                                    bomAlloc.getQuantity(), shelfAvailableQuantity));
//                        }
//                    }
//
//                    // 创建可用数量信息
//                    OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO availableDTO = new OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO();
//                    availableDTO.setComponentProductId(bomAlloc.getComponentProductId());
//                    availableDTO.setBatchNo(bomAlloc.getBatchNo());
//                    availableDTO.setShelfId(bomAlloc.getShelfId());
//                    availableDTO.setAvailableQuantity(shelfAvailableQuantity);
//                    availableDTO.setAllocatedQuantity(allocatedQuantity);
//                    availableDTO.setBatchAvailableQuantity(batchAvailableQuantity);
//                    availableList.add(availableDTO);
//                    productBatchShelf2CountMap.put(key, shelfAvailableQuantity);
//                    productBatch2CountMap.put(keyOfBatch, allocatedQuantity);
//                }
//            }
//
//        }
//
//        response.setSuccess(!hasInsufficient);
//        response.setMessage(hasInsufficient ? errorMessage.toString() : "分配数量合理");
//        response.setAvailableQuantities(availableList);
//
//        return response;
//    }


    private String buildBatchShelfKey(Long productId, String batchNo, Long shelfId) {
        return productId + "_" + batchNo + "_" + shelfId;
    }
    /**
     * 获取实时可用数量
     */
//    public List<OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO> getRealTimeAvailableQuantities(OutBoundBatchAllocationCheckRequest request) {
//        log.info("开始计算实时可用数量，仓库ID: {}, 商品项数量: {}",
//                request.getWarehouseId(), request.getProductAllocations().size());
//
//        // 1. 获取所有涉及的原料商品ID
//        Set<Long> componentProductIds = extractComponentProductIds(request);
//        if (componentProductIds.isEmpty()) {
//            log.info("未找到原料商品ID");
//            return new ArrayList<>();
//        }
//        List<ShelfZone> warehouseShelves = shelfService.selectByTenantId(request.getTenantId());
//        Map<Long, ShelfZone> shelfId2ShelfMap = warehouseShelves.stream()
//                .collect(Collectors.toMap(ShelfZone::getId, v -> v));
//
//
//        // 2. 获取批次货架的实际库存
//        Map<String, InventoryShelf> stockMap = getBatchShelfStockMap(request.getWarehouseId(), componentProductIds,  request.getTenantId());
//
//        // 3. 计算每个批次货架的总分配量（排除当前操作的商品） TODO yang 1111111
//        Map<String, BigDecimal> allocationMap = calculateTotalAllocation(request, request.getCurrentProductId(),null);
//
//        // 4. 构建实时可用数量结果
//        List<OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO> availableList = buildAvailableQuantities(stockMap, allocationMap, shelfId2ShelfMap);
//
//        log.info("实时可用数量计算完成，共 {} 条记录", availableList.size());
//        return availableList;
//    }

    /**
     * 提取所有涉及的原料商品ID
     */
    private Set<Long> extractComponentProductIds(OutBoundBatchAllocationCheckRequest request) {
        return request.getProductAllocations().stream()
                .filter(item -> item.getBomAllocations() != null)
                .flatMap(item -> item.getBomAllocations().stream())
                .map(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getComponentProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 获取批次货架库存映射
     */
    private Map<String, InventoryShelf> getBatchShelfStockMap(Long warehouseId, Set<Long> productIds, Long tenantId) {
        List<InventoryShelf> stockList = inventoryShelfService.getBatchShelfStock(tenantId, warehouseId, productIds);

        Map<String, InventoryShelf> stockMap = new HashMap<>();
        for (InventoryShelf stock : stockList) {
            String key = buildBatchShelfKey(stock.getProductId(), stock.getBatchNo(), stock.getShelfId());
            stockMap.put(key, stock);
        }

        log.info("获取到 {} 个批次货架的库存信息", stockMap.size());
        return stockMap;
    }

    /**
     * 构建成品-原料层级结构
     */
    private List<OutBoundBatchAllocationCheckResponse.ProductParentDto> buildProductHierarchy(
            List<Product> products,
            List<ProductBom> productBoms,
            List<OutBoundBatchAllocationCheckRequest.ProductAllocationDTO> productAllocations) {

        List<OutBoundBatchAllocationCheckResponse.ProductParentDto> parentList = new ArrayList<>();

        for (OutBoundBatchAllocationCheckRequest.ProductAllocationDTO allocation : productAllocations) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(allocation.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (product == null) continue;

            OutBoundBatchAllocationCheckResponse.ProductParentDto parentDto = new OutBoundBatchAllocationCheckResponse.ProductParentDto();
            parentDto.setProductParentId(product.getId());
            parentDto.setProductParentName(product.getName());
            parentDto.setProductSonDtoList(new ArrayList<>());

            // 查找该成品的BOM原料
            ProductBom productBom = productBoms.stream()
                    .filter(bom -> bom.getProductId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (productBom != null) {
                // 这里可以根据需要添加原料信息到productSonDtoList
                // 实际实现需要根据BOM详情查询原料信息
            }

            parentList.add(parentDto);
        }

        return parentList;
    }

    /**
     * 计算剩余数量
     */
//    private void calculateTotalAllocation(OutBoundBatchAllocationCheckRequest request,
//                                                             Long currentProductId,
//                                                             Map<String, BigDecimal> productBatchShelf2CountMap,
//                                                             Map<String, BigDecimal> productBatch2CountMap) {
//        //先算不是这个商品的总数量
//        for (int i = 0; i < request.getProductAllocations().size(); i++) {
//            OutBoundBatchAllocationCheckRequest.ProductAllocationDTO p = request.getProductAllocations().get(i);
//            //跳过当前商品
//            if (currentProductId != null && currentProductId.equals(p.getProductId())) {
//                continue;
//            }
//            for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bom : p.getBomAllocations()) {
//                if (bom.getQuantity().compareTo(BigDecimal.valueOf(0)) <= 0) {
//                    continue;
//                }
//                //减去产品批次货架数量
//                String key = buildBatchShelfKey(bom.getComponentProductId(), bom.getBatchNo(), bom.getShelfId());
//                BigDecimal countOfBeforSub = productBatchShelf2CountMap.getOrDefault(key, BigDecimal.ZERO);
//                BigDecimal subtractOfShelf = countOfBeforSub.subtract(bom.getQuantity());
//                productBatchShelf2CountMap.put(key, subtractOfShelf);
//
//
//                //减去产品批次数量
//                String key2 = buildBatchShelfKey(bom.getComponentProductId(), bom.getBatchNo(), -1L);
//                BigDecimal countOfBeforSub2 = productBatch2CountMap.getOrDefault(key2, BigDecimal.ZERO);
//                BigDecimal subtractOfBatch = countOfBeforSub2.subtract(bom.getQuantity());
//                productBatch2CountMap.put(key2, subtractOfBatch);
//            }
//        }
//    }
    public void sortBatchAllocations(OutBoundBatchAllocationCheckRequest request,
                                     List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO> batchAllocations) {

        batchAllocations.sort((o1, o2) -> {
            boolean o1Matches = matchesCriteria(o1, request);
            boolean o2Matches = matchesCriteria(o2, request);

            if (o1Matches && !o2Matches) {
                return -1;
            } else if (!o1Matches && o2Matches) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    private boolean matchesCriteria(OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc,
                                    OutBoundBatchAllocationCheckRequest request) {
        return (bomAlloc.getBatchNo() != null && bomAlloc.getBatchNo().equals(request.getCurrentBatchNo())) &&
                (bomAlloc.getShelfId() != null && bomAlloc.getShelfId().equals(request.getCurrentShelfId()));
    }

    //    新的
    public OutBoundBatchAllocationCheckResponse checkBatchAllocation(OutBoundBatchAllocationCheckRequest request) {
        OutBoundBatchAllocationCheckResponse response = new OutBoundBatchAllocationCheckResponse();

        // 1. 获取所有涉及的原料商品ID
        Set<Long> componentProductIds = request.getProductAllocations().stream()
                .flatMap(item -> item.getBomAllocations().stream())
                .map(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getComponentProductId)
                .collect(Collectors.toSet());

        // 2. 查询数据库获取实际库存
        List<InventoryShelf> inventoryList = inventoryShelfService.getBatchShelfStock(
                request.getTenantId(), request.getWarehouseId(), componentProductIds);

        // 3.1. 构建批次货架映射
        Map<String, BigDecimal> productBatchShelf2CountMap = inventoryList.stream().collect(Collectors.toMap(
                stock -> buildBatchShelfKey(stock.getProductId(), stock.getBatchNo(), stock.getShelfId()),
                InventoryShelf::getQuantity
        ));

        // 3.2. 构建批次映射
        Map<String, BigDecimal> productBatch2CountMap = inventoryList.stream()
                .filter(inventory -> inventory.getProductId() != null && inventory.getBatchNo() != null)
                .collect(Collectors.toMap(
                        inventory -> buildBatchShelfKey(inventory.getProductId(), inventory.getBatchNo(), -1L),
                        inventory -> inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO,
                        BigDecimal::add
                ));

        // 4. 计算每个批次货架的剩余数量（扣除其他成品的分配）
        calculateTotalAllocation(request, request.getCurrentProductId(), productBatchShelf2CountMap, productBatch2CountMap);

        // 5. 构建返回结果
        List<OutBoundBatchAllocationCheckResponse.ProductParentDto> productParentList = new ArrayList<>();
        boolean hasInsufficient = false;
        StringBuilder errorMessage = new StringBuilder();

        // 5.0. 构建当前产品的批次货架数据
        List<OutBoundBatchAllocationCheckResponse.ProductParentDto> productParentOfCurrentList = new ArrayList<>();
        // 5.1.先构建当前产品的批次货架数据，然后构建出返回对象OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO，
        // 并且从productBatchShelf2CountMap，productBatch2CountMap扣减库存
        for (OutBoundBatchAllocationCheckRequest.ProductAllocationDTO productAlloc : request.getProductAllocations()) {
            // 处理该批次下的所有货架分配
            boolean isCurrentProduct = request.getCurrentProductId() != null &&
                    request.getCurrentProductId().equals(productAlloc.getProductId());
            if (!isCurrentProduct) {
                continue;
            }

            OutBoundBatchAllocationCheckResponse.ProductParentDto parentDto = new OutBoundBatchAllocationCheckResponse.ProductParentDto();
            parentDto.setProductParentId(productAlloc.getProductId());
            parentDto.setProductParentName(productAlloc.getProductName());
            parentDto.setProductSonDtoList(new ArrayList<>());

            // 按原料分组处理
            Map<Long, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> componentGroupMap =
                    productAlloc.getBomAllocations().stream()
                            .collect(Collectors.groupingBy(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getComponentProductId));

            for (Map.Entry<Long, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> entry : componentGroupMap.entrySet()) {
                Long componentProductId = entry.getKey();
                List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO> bomAllocations = entry.getValue();

                // 获取原料信息（从第一个分配记录中获取）
                OutBoundBatchAllocationCheckRequest.BomAllocationDTO firstBomAlloc = bomAllocations.get(0);

                OutBoundBatchAllocationCheckResponse.ProductSonDto sonDto = new OutBoundBatchAllocationCheckResponse.ProductSonDto();
                sonDto.setProductParentId(productAlloc.getProductId());
                sonDto.setProductParentName(productAlloc.getProductName());
                sonDto.setProductSonId(componentProductId);
                sonDto.setProductSonName(firstBomAlloc.getComponentProductName());
                sonDto.setBatchList(new ArrayList<>());

                //补充： 先算出其他货架的商品总和
                BigDecimal totalOtherShelfAllocatedInBatch = BigDecimal.ZERO;
                for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc : bomAllocations) {
                    if (bomAlloc.getShelfId() != null
                            && StringUtils.isNotBlank(bomAlloc.getBatchNo())
                            && (!Objects.equals(bomAlloc.getShelfId(), request.getCurrentShelfId()) || !bomAlloc.getBatchNo().equals(request.getCurrentBatchNo()))) {
                        totalOtherShelfAllocatedInBatch = totalOtherShelfAllocatedInBatch.add(bomAlloc.getQuantity());
                    }
                }

                // 按批次分组处理
                Map<String, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> batchGroupMap =
                        bomAllocations.stream()
                                .collect(Collectors.groupingBy(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getBatchNo));

                for (Map.Entry<String, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> batchEntry : batchGroupMap.entrySet()) {
                    String batchNo = batchEntry.getKey();
                    List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO> batchAllocations = batchEntry.getValue();

                    // 创建批次级别的DTO
                    OutBoundBatchAllocationCheckResponse.BatchCountDTO batchCountDTO = new OutBoundBatchAllocationCheckResponse.BatchCountDTO();
                    batchCountDTO.setProductParentId(productAlloc.getProductId());
                    batchCountDTO.setProductParentName(productAlloc.getProductName());
                    batchCountDTO.setProductSonId(componentProductId);
                    batchCountDTO.setProductSonName(firstBomAlloc.getComponentProductName());
                    batchCountDTO.setBatchNo(batchNo);
                    batchCountDTO.setShelfList(new ArrayList<>());

                    // 计算批次总可用数量 这里先默认设置)
                    String batchKey = buildBatchShelfKey(componentProductId, batchNo, -1L);
                    BigDecimal batchAvailableQuantity = productBatch2CountMap.getOrDefault(batchKey, BigDecimal.ZERO);
                    batchCountDTO.setAvailableBatchQuantity(batchAvailableQuantity);

                    // 用于跟踪当前批次的总分配量
                    BigDecimal totalAllocatedInBatch = BigDecimal.ZERO;
                    // batchAllocations 排序，按照 bomAlloc.getBatchNo()等于 request.getCurrentBatchNo()， bomAlloc.getShelfId() 等于 request.getCurrentShelfId() 的放在最前面
                    sortBatchAllocations(request, batchAllocations);

                    for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc : batchAllocations) {
                        if (bomAlloc.getShelfId() != null
                                && StringUtils.isNotBlank(bomAlloc.getBatchNo())
                                && Objects.equals(bomAlloc.getShelfId(), request.getCurrentShelfId())
                                && bomAlloc.getBatchNo().equals(request.getCurrentBatchNo())) {
                            String shelfKey = buildBatchShelfKey(componentProductId, batchNo, bomAlloc.getShelfId());
                            BigDecimal remainCount = productBatchShelf2CountMap.getOrDefault(shelfKey, BigDecimal.ZERO);

                            BigDecimal allocatedQuantity = BigDecimal.ZERO;
                            BigDecimal shelfAvailableQuantity = remainCount;

                            //获取总量扣减可分配数量
                            BigDecimal totalBomQuantity = new BigDecimal(0);
                            if (bomAlloc.getTotalBomQuantity().subtract(totalOtherShelfAllocatedInBatch).compareTo(bomAlloc.getQuantity()) > 0) {
                                //bomAlloc.getTotalBomQuantity()（总量） - 其他货架的商品综总和 > bomAlloc.getQuantity()（本货架的数量）,则使用 bomAlloc.getQuantity()
                                totalBomQuantity = bomAlloc.getQuantity();
                            } else {
                                // bomAlloc.getTotalBomQuantity()（总量） - 其他货架的商品综总和 < bomAlloc.getQuantity()（本货架的数量）,则使用  totalBomQuantity（总量） - 其他货架的商品综总和
                                totalBomQuantity = bomAlloc.getTotalBomQuantity().subtract(totalOtherShelfAllocatedInBatch);
                            }


                            // 当前成品进行分配计算
                            if (totalBomQuantity.compareTo(remainCount) > 0) {
                                //如果总量扣减可分配数量 > 库存 数量， 分配数量使用库存数量
                                // 分配数量超过库存，实际分配最大库存数量
                                allocatedQuantity = remainCount;
                                shelfAvailableQuantity = BigDecimal.ZERO;
                                totalAllocatedInBatch = totalAllocatedInBatch.add(allocatedQuantity);
                                hasInsufficient = true;
                                errorMessage.append(String.format("成品%s的原料%s批次%s货架%s: 分配%s > 剩余%s，实际分配%s; ",
                                        productAlloc.getProductName(), bomAlloc.getComponentProductName(),
                                        batchNo, bomAlloc.getShelfName(),
                                        bomAlloc.getQuantity(), remainCount, allocatedQuantity));
                            } else {
                                //如果总量扣减可分配数量 < 库存 数量， 则使用 总量扣减可分配数量
                                // 分配数量在可用范围内
                                allocatedQuantity = totalBomQuantity;
                                shelfAvailableQuantity = remainCount.subtract(allocatedQuantity);
                                totalAllocatedInBatch = totalAllocatedInBatch.add(allocatedQuantity);
                            }


                            // 更新批次库存映射
                            productBatchShelf2CountMap.put(shelfKey, shelfAvailableQuantity);

                            BigDecimal batchAvailableQuantityOfJisuan = batchAvailableQuantity.subtract(allocatedQuantity);
                            // 确保批次可用数量不为负数
                            if (batchAvailableQuantityOfJisuan.compareTo(BigDecimal.ZERO) < 0) {
                                batchAvailableQuantityOfJisuan = BigDecimal.ZERO;
                            }
                            productBatch2CountMap.put(batchKey, batchAvailableQuantityOfJisuan);

//                            // 更新批次可用数量
//                            String batchKeyOfJisuan = buildBatchShelfKey(componentProductId, batchNo, -1L);
//                            BigDecimal batchAvailableQuantityOfJisuan = productBatch2CountMap.getOrDefault(batchKeyOfJisuan, BigDecimal.ZERO);
//                            BigDecimal updatedBatchAvailableQuantity = batchAvailableQuantityOfJisuan.subtract(allocatedQuantity);
//                            // 确保批次可用数量不为负数
//                            if (updatedBatchAvailableQuantity.compareTo(BigDecimal.ZERO) < 0) {
//                                updatedBatchAvailableQuantity = BigDecimal.ZERO;
//                            }
//                            productBatch2CountMap.put(batchKey, updatedBatchAvailableQuantity);

                            // 创建货架分配信息
                            OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO shelfAvailableDTO =
                                    new OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO();
                            shelfAvailableDTO.setProductParentId(productAlloc.getProductId());
                            shelfAvailableDTO.setProductParentName(productAlloc.getProductName());
                            shelfAvailableDTO.setProductSonId(componentProductId);
                            shelfAvailableDTO.setProductSonName(bomAlloc.getComponentProductName());
                            shelfAvailableDTO.setBatchNo(batchNo);
                            shelfAvailableDTO.setShelfId(bomAlloc.getShelfId());
                            shelfAvailableDTO.setShelfName(bomAlloc.getShelfName());
                            shelfAvailableDTO.setShelfAvailableQuantity(shelfAvailableQuantity);
                            shelfAvailableDTO.setAllocatedQuantity(allocatedQuantity);

                            batchCountDTO.getShelfList().add(shelfAvailableDTO);
                        } else {
                            OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO shelfAvailableDTO =
                                    new OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO();
                            shelfAvailableDTO.setProductParentId(productAlloc.getProductId());
                            shelfAvailableDTO.setProductParentName(productAlloc.getProductName());
                            shelfAvailableDTO.setProductSonId(componentProductId);
                            shelfAvailableDTO.setProductSonName(bomAlloc.getComponentProductName());
                            shelfAvailableDTO.setBatchNo(batchNo);
                            shelfAvailableDTO.setShelfId(bomAlloc.getShelfId());
                            shelfAvailableDTO.setShelfName(bomAlloc.getShelfName());
//                            shelfAvailableDTO.setShelfAvailableQuantity(shelfAvailableQuantity);
//                            shelfAvailableDTO.setAllocatedQuantity(allocatedQuantity);

                            //更新批次库存数量
                            String shelfKey = buildBatchShelfKey(componentProductId, batchNo, bomAlloc.getShelfId());
                            BigDecimal remainCount = productBatchShelf2CountMap.getOrDefault(shelfKey, BigDecimal.ZERO);
                            BigDecimal remainCountAfterAlloc = remainCount.subtract(bomAlloc.getQuantity());
                            productBatchShelf2CountMap.put(shelfKey, remainCountAfterAlloc);

                            // 更新批次可用数量
                            String batchKeyOfJisuan = buildBatchShelfKey(componentProductId, batchNo, -1L);
                            BigDecimal batchAvailableQuantityOfJisuan = productBatch2CountMap.getOrDefault(batchKeyOfJisuan, BigDecimal.ZERO);
                            BigDecimal updatedBatchAvailableQuantity = batchAvailableQuantityOfJisuan.subtract(bomAlloc.getQuantity());
                            // 确保批次可用数量不为负数
                            if (updatedBatchAvailableQuantity.compareTo(BigDecimal.ZERO) < 0) {
                                updatedBatchAvailableQuantity = BigDecimal.ZERO;
                            }
                            productBatch2CountMap.put(batchKey, updatedBatchAvailableQuantity);

                            shelfAvailableDTO.setShelfAvailableQuantity(remainCountAfterAlloc);
                            shelfAvailableDTO.setAllocatedQuantity(bomAlloc.getQuantity());
                            batchCountDTO.getShelfList().add(shelfAvailableDTO);
                        }

                    }

                    String batchKeyOfJisuan = buildBatchShelfKey(componentProductId, batchNo, -1L);
                    BigDecimal batchAvailableQuantityOfJisuan = productBatch2CountMap.getOrDefault(batchKeyOfJisuan, BigDecimal.ZERO);

                    batchCountDTO.setAvailableBatchQuantity(batchAvailableQuantityOfJisuan);

                    sonDto.getBatchList().add(batchCountDTO);
                }

                parentDto.getProductSonDtoList().add(sonDto);
            }

            productParentOfCurrentList.add(parentDto);
        }

        // 6. 按成品分组处理
        for (OutBoundBatchAllocationCheckRequest.ProductAllocationDTO productAlloc : request.getProductAllocations()) {
            // 处理该批次下的所有货架分配
            boolean isCurrentProduct = request.getCurrentProductId() != null &&
                    request.getCurrentProductId().equals(productAlloc.getProductId());
            if (isCurrentProduct) {
                productParentList.addAll(productParentOfCurrentList);
                continue;
            }
            OutBoundBatchAllocationCheckResponse.ProductParentDto parentDto = new OutBoundBatchAllocationCheckResponse.ProductParentDto();
            parentDto.setProductParentId(productAlloc.getProductId());
            parentDto.setProductParentName(productAlloc.getProductName());
            parentDto.setProductSonDtoList(new ArrayList<>());

            // 按原料分组处理
            Map<Long, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> componentGroupMap =
                    productAlloc.getBomAllocations().stream()
                            .collect(Collectors.groupingBy(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getComponentProductId));

            for (Map.Entry<Long, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> entry : componentGroupMap.entrySet()) {
                Long componentProductId = entry.getKey();
                List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO> bomAllocations = entry.getValue();

                // 获取原料信息（从第一个分配记录中获取）
                OutBoundBatchAllocationCheckRequest.BomAllocationDTO firstBomAlloc = bomAllocations.get(0);

                OutBoundBatchAllocationCheckResponse.ProductSonDto sonDto = new OutBoundBatchAllocationCheckResponse.ProductSonDto();
                sonDto.setProductParentId(productAlloc.getProductId());
                sonDto.setProductParentName(productAlloc.getProductName());
                sonDto.setProductSonId(componentProductId);
                sonDto.setProductSonName(firstBomAlloc.getComponentProductName());
                sonDto.setBatchList(new ArrayList<>());

                // 按批次分组处理
                Map<String, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> batchGroupMap =
                        bomAllocations.stream()
                                .collect(Collectors.groupingBy(OutBoundBatchAllocationCheckRequest.BomAllocationDTO::getBatchNo));

                for (Map.Entry<String, List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO>> batchEntry : batchGroupMap.entrySet()) {
                    String batchNo = batchEntry.getKey();
                    List<OutBoundBatchAllocationCheckRequest.BomAllocationDTO> batchAllocations = batchEntry.getValue();

                    // 创建批次级别的DTO
                    OutBoundBatchAllocationCheckResponse.BatchCountDTO batchCountDTO = new OutBoundBatchAllocationCheckResponse.BatchCountDTO();
                    batchCountDTO.setProductParentId(productAlloc.getProductId());
                    batchCountDTO.setProductParentName(productAlloc.getProductName());
                    batchCountDTO.setProductSonId(componentProductId);
                    batchCountDTO.setProductSonName(firstBomAlloc.getComponentProductName());
                    batchCountDTO.setBatchNo(batchNo);
                    batchCountDTO.setShelfList(new ArrayList<>());

                    // 计算批次总可用数量
                    String batchKey = buildBatchShelfKey(componentProductId, batchNo, -1L);
                    BigDecimal batchAvailableQuantity = productBatch2CountMap.getOrDefault(batchKey, BigDecimal.ZERO);
                    batchCountDTO.setAvailableBatchQuantity(batchAvailableQuantity);


                    // 用于跟踪当前批次的总分配量
                    BigDecimal totalAllocatedInBatch = BigDecimal.ZERO;

                    for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bomAlloc : batchAllocations) {
                        String shelfKey = buildBatchShelfKey(componentProductId, batchNo, bomAlloc.getShelfId());
                        BigDecimal remainCount = productBatchShelf2CountMap.getOrDefault(shelfKey, BigDecimal.ZERO);

                        BigDecimal allocatedQuantity = BigDecimal.ZERO;
                        BigDecimal shelfAvailableQuantity = remainCount;

                        // 其他成品，只显示当前库存状态，不进行分配计算
                        allocatedQuantity = bomAlloc.getQuantity();

                        // 创建货架分配信息
                        OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO shelfAvailableDTO =
                                new OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO();
                        shelfAvailableDTO.setProductParentId(productAlloc.getProductId());
                        shelfAvailableDTO.setProductParentName(productAlloc.getProductName());
                        shelfAvailableDTO.setProductSonId(componentProductId);
                        shelfAvailableDTO.setProductSonName(bomAlloc.getComponentProductName());
                        shelfAvailableDTO.setBatchNo(batchNo);
                        shelfAvailableDTO.setShelfId(bomAlloc.getShelfId());
                        shelfAvailableDTO.setShelfName(bomAlloc.getShelfName());
                        shelfAvailableDTO.setShelfAvailableQuantity(shelfAvailableQuantity);
                        shelfAvailableDTO.setAllocatedQuantity(allocatedQuantity);

                        batchCountDTO.getShelfList().add(shelfAvailableDTO);
                    }

                    // 更新批次可用数量
                    if (isCurrentProduct) {
                        BigDecimal updatedBatchAvailableQuantity = batchAvailableQuantity.subtract(totalAllocatedInBatch);
                        // 确保批次可用数量不为负数
                        if (updatedBatchAvailableQuantity.compareTo(BigDecimal.ZERO) < 0) {
                            updatedBatchAvailableQuantity = BigDecimal.ZERO;
                        }
                        productBatch2CountMap.put(batchKey, updatedBatchAvailableQuantity);
                        batchCountDTO.setAvailableBatchQuantity(updatedBatchAvailableQuantity);
                    }

                    sonDto.getBatchList().add(batchCountDTO);
                }

                parentDto.getProductSonDtoList().add(sonDto);
            }

            productParentList.add(parentDto);
        }

        response.setSuccess(!hasInsufficient);
        response.setMessage(hasInsufficient ? errorMessage.toString() : "分配数量合理");
        response.setBatchAllocatedList(productParentList);

        return response;
    }


    /**
     * 获取批次及批次创建的时间
     */
    public Map<String, Date> getBatch2BatchCreateDate(Long tenantId, List<String> batchNos) {
        List<InboundOrderItem> inboundOrderItems = inboundOrderItemService.selectByBatNoList(tenantId, batchNos);
        //获取 batNo 对应的 createdAt
        return inboundOrderItems.stream()
                .collect(Collectors.toMap(InboundOrderItem::getBatchNo, InboundOrderItem::getCreatedAt, (key1, key2) -> key1));
    }
    /**
     * 计算剩余数量 - 确保不为负数
     */
    private void calculateTotalAllocation(OutBoundBatchAllocationCheckRequest request,
                                          Long currentProductId,
                                          Map<String, BigDecimal> productBatchShelf2CountMap,
                                          Map<String, BigDecimal> productBatch2CountMap) {
        for (OutBoundBatchAllocationCheckRequest.ProductAllocationDTO p : request.getProductAllocations()) {
            // 跳过当前商品
            if (currentProductId != null && currentProductId.equals(p.getProductId())) {
                continue;
            }
            for (OutBoundBatchAllocationCheckRequest.BomAllocationDTO bom : p.getBomAllocations()) {
                if (bom.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                // 减去产品批次货架数量
                String key = buildBatchShelfKey(bom.getComponentProductId(), bom.getBatchNo(), bom.getShelfId());
                BigDecimal countOfBeforeSub = productBatchShelf2CountMap.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal subtractOfShelf = countOfBeforeSub.subtract(bom.getQuantity());
                // 确保货架数量不为负数
                if (subtractOfShelf.compareTo(BigDecimal.ZERO) < 0) {
                    subtractOfShelf = BigDecimal.ZERO;
                }
                productBatchShelf2CountMap.put(key, subtractOfShelf);

                // 减去产品批次数量
                String key2 = buildBatchShelfKey(bom.getComponentProductId(), bom.getBatchNo(), -1L);
                BigDecimal countOfBeforeSub2 = productBatch2CountMap.getOrDefault(key2, BigDecimal.ZERO);
                BigDecimal subtractOfBatch = countOfBeforeSub2.subtract(bom.getQuantity());
                // 确保批次数量不为负数
                if (subtractOfBatch.compareTo(BigDecimal.ZERO) < 0) {
                    subtractOfBatch = BigDecimal.ZERO;
                }
                productBatch2CountMap.put(key2, subtractOfBatch);
            }
        }
    }


    public List<InventoryAlertResp> getInventoryAlerts(InventoryAlertReq req) {
        List<Product> products = productService.selectByTenantId(req.getTenantId());

        Map<Long, BigDecimal> product2InventoryQuantity = new HashMap<>();
        if (req.getWarehouseId() == null) {
            List<Inventory> inventories = inventoryService.selectByTenantId(req.getTenantId());
            product2InventoryQuantity = inventories.stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getQuantity()));
        } else {
            List<InventoryWarehouse> inventoryWarehouses = inventoryWarehouseService.selectByWarehourseId(req.getWarehouseId(), req.getTenantId());
            product2InventoryQuantity = inventoryWarehouses.stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getQuantity()));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = wareHouseService.selectByTenantId(req.getTenantId());
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        List<InventoryAlertResp> inventoryAlerts = new ArrayList<>();

        for (Product product : products) {
            Long minStock = product.getMinStock();
            BigDecimal minStockBig = minStock == null ? BigDecimal.ZERO : new BigDecimal(minStock);
            BigDecimal inventoryQuantity = product2InventoryQuantity.getOrDefault(product.getId(), BigDecimal.ZERO);
//            if (inventoryQuantity == null || inventoryQuantity.compareTo(new BigDecimal(minStock)) <= 0) {
            InventoryAlertResp r = new InventoryAlertResp();
            r.setProductId(product.getId());
            r.setProductName(product.getName());
            r.setSku(product.getSku());
            r.setSpec(product.getSpec());
            r.setColor(product.getColor());
            r.setWarehouseId(req.getWarehouseId());
            r.setWarehouseName(warehouseId2WarehouseMap.getOrDefault(req.getWarehouseId(), new Warehouse()).getName());
            r.setCurrentStock(inventoryQuantity);
            r.setWarningThreshold(product.getMinStock() == null ? BigDecimal.ZERO : new BigDecimal(product.getMinStock()));
            r.setUrgentThreshold(product.getMinStock() == null ? BigDecimal.ZERO : new BigDecimal(product.getMinStock()).divide(new BigDecimal(2), RoundingMode.HALF_UP));
            String level = CkInventoryEnums.ChartsWarningLevel.NORMAL.getCode();
            Integer levelSort = CkInventoryEnums.ChartsWarningLevel.NORMAL.getSort();
            if (inventoryQuantity.compareTo(r.getWarningThreshold()) > 0) {
                level = CkInventoryEnums.ChartsWarningLevel.NORMAL.getCode();
                levelSort = CkInventoryEnums.ChartsWarningLevel.NORMAL.getSort();
            } else if (inventoryQuantity.compareTo(r.getUrgentThreshold()) > 0) {
                level = CkInventoryEnums.ChartsWarningLevel.WARNING.getCode();
                levelSort = CkInventoryEnums.ChartsWarningLevel.WARNING.getSort();
            } else {
                level = CkInventoryEnums.ChartsWarningLevel.URGENT.getCode();
                levelSort = CkInventoryEnums.ChartsWarningLevel.URGENT.getSort();
            }
            r.setAlertLevel(level);
            r.setAlertLevelSort(levelSort);
            //r.setLastUpdateTime(DateUtil.formatDateTime(inventory.getModifiedAt()));

            if (StringUtils.isNotBlank(req.getAlertLevel())) {
                if (!r.getAlertLevel().equals(req.getAlertLevel())) {
                    continue;
                }
            }
            inventoryAlerts.add(r);
//            }
        }

        inventoryAlerts.sort(Comparator.comparingInt(InventoryAlertResp::getAlertLevelSort));
        return inventoryAlerts;
    }

    public InventoryCountsOfIndexPageResp countsOfIndexPage(Long tenantId) {
        InventoryCountsOfIndexPageResp r = new InventoryCountsOfIndexPageResp();

        Long totalProducts = productService.selectCountsOfProducts(tenantId);
        r.setTotalProducts(totalProducts);

        Long totalWarehouses = wareHouseService.selectCountsOfWareHouses(tenantId);
        r.setTotalWarehouses(totalWarehouses);

        Long todayInbound = inboundOrderService.selectCountsOfInboundOrders(tenantId, new Date());
        r.setTodayInbound(todayInbound);

        Long todayOutbound = outboundOrderService.selectCountsOfOutboundOrders(tenantId, new Date());
        r.setTodayOutbound(todayOutbound);

        Long inboundApproval = inboundOrderService.selectCountsOfApprovals(tenantId);
        r.setTodoInBoundApproval(inboundApproval);
        Long outboundApproval = outboundOrderService.selectCountsOfApprovals(tenantId);
        r.setTodoOutBoundApproval(outboundApproval);

        List<Product> products = productService.selectByTenantId(tenantId);
        List<Inventory> inventories = inventoryService.selectByTenantId(tenantId);
        Map<Long, BigDecimal> productId2InventoryQuantityMap = inventories.stream().collect(Collectors.toMap(v -> v.getProductId(), v -> v.getQuantity()));
        Long lowStock = 0L;
        for (Product product : products) {
            BigDecimal inventoryQuantity = productId2InventoryQuantityMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal minStock = product.getMinStock() == null ? BigDecimal.ZERO : new BigDecimal(product.getMinStock());
            if (minStock.compareTo(inventoryQuantity) >= 0) {
                lowStock += 1;
            }
        }
        r.setLowStock(lowStock);

        return r;
    }

    public List<ProductUsedShelfResp> getCommonlyUsedShelvesForGoods(List<Long> productIds, Long tenantId) {
        List<ProductUsedShelfResp> productUsedShelfResps = new ArrayList<>();
        if (CollectionUtils.isEmpty(productIds)) {
            return productUsedShelfResps;
        }
        List<InventoryShelf> inventoryShelves = inventoryShelfService.selectByProductIds(productIds, tenantId);
        if (CollectionUtils.isEmpty(inventoryShelves)) {
            return productUsedShelfResps;
        }
        Map<Long, List<InventoryShelf>> productId2ShelfInventoryMap = inventoryShelves.stream().collect(Collectors.groupingBy(InventoryShelf::getProductId));

        for (Long productId : productId2ShelfInventoryMap.keySet()) {
            List<InventoryShelf> inventoryShelfList = productId2ShelfInventoryMap.get(productId);
            ProductUsedShelfResp r = new ProductUsedShelfResp();
            r.setProductId(productId);
            r.setShelfIds(inventoryShelfList.stream().map(InventoryShelf::getShelfId).collect(Collectors.toList()));
            productUsedShelfResps.add(r);
        }
        return productUsedShelfResps;
    }
}
