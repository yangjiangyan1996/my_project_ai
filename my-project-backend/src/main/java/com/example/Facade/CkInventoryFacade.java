package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.resp.InventoryBatchResp;
import com.example.entity.cangku.resp.InventoryListResp;
import com.example.entity.cangku.resp.InventoryPageListResp;
import com.example.enums.CkCommonEnums;
import com.example.enums.CkInventoryEnums;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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
    CkProductCategoryService productCategoryService;
    @Resource
    CkProductService productService;
    @Resource
    CkWareHouseService wareHouseService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
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
        List<Long> outboundOrderIds = inboundOrderItems.stream()
                .map(InboundOrderItem::getOrderId)
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


        // 构建响应列表
        List<InventoryPageListResp> resultList = productInventoryMap.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();

                    // 获取产品信息
                    Product product = productMap.getOrDefault(productId, new Product());

                    // 构建响应对象
                    InventoryPageListResp r = new InventoryPageListResp();
                    r.setId(productId);
                    r.setProductName(product.getName());
                    r.setSpec(product.getSpec());
                    r.setColor(product.getColor());
                    r.setUnitName(unitCode2UnitMap.get(product.getUnitCode()).getUnitName());

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
                    r.setWarehouseInventoryList(warehouse2InfoMap.values().stream().toList());


                    List<OutboundOrderItem> outboundList = productId2OutboundItemListMap.getOrDefault(productId, new ArrayList<>());
                    Map<Long, InventoryPageListResp.WarehouseInventory> outwarehouse2InfoMap = new HashMap<>();
                    for (OutboundOrderItem item : outboundList) {
                        Long orderId = item.getOrderId();
                        OutboundOrder outboundOrder = outboundOrderId2InfoMap.getOrDefault(orderId, new OutboundOrder());
                        if (!outboundOrder.getStatus().equals(3)) {
                            continue;
                        }

                        Long warehouseId = outboundOrder.getWarehouseId();
                        InventoryPageListResp.WarehouseInventory w = null;
                        w.setWarehouseId(warehouseId);
                        w.setWarehouseName(warehouseMap.getOrDefault(outboundOrder.getWarehouseId(), new Warehouse()).getName());
                        w.setQuantity(item.getQuantity());

                        if (warehouse2InfoMap.containsKey(warehouseId)) {
                            w = warehouse2InfoMap.get(warehouseId);
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
                    r.setOutUnitTotalNum(totalOutboundQuantityOfAllWarehouses.divide(product.getOutUnitPerNum()));

                    //体积 = 出货单位的长宽高 * 出货单位数量的总数量
                    //TODO 数据OK了，把这里要替换
                    r.setVolume(BigDecimal.ZERO);
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
            r.setUnitName(unitCode2UnitMap.get(productMap.get(productId).getUnitCode()).getUnitName());
            r.setAvailableQuantity(inventoryWarehouse.getQuantity().subtract(inventoryWarehouse.getLockedQuantity()));
            r.setQuantity(inventoryWarehouse.getQuantity());
            r.setLockedQuantity(inventoryWarehouse.getLockedQuantity());
            r.setOutUnitName(unitCode2UnitMap.get(productMap.get(productId).getOutUnitCode()).getUnitName());
            r.setOutUnitPerNum(productMap.get(productId).getOutUnitPerNum());
            r.setCategoryName(productCode2CategoryMap.get(productMap.get(productId).getCategoryCode()).getCategoryName());
            result.add(r);
        }

        return result;
    }

    public List<InventoryBatchResp> batches(Long warehouseId, Long productId, Long tenantId) {
        List<InventoryBatch> inventoryBatches = inventoryBatchService.selectByWarehouseIdAndProductId(warehouseId, productId, tenantId);
        if (CollectionUtils.isEmpty(inventoryBatches)) {
            return Collections.emptyList();
        }
        return inventoryBatches.stream().map(c->{
            InventoryBatchResp resp = new InventoryBatchResp();
            resp.setQuantity(c.getQuantity());
            resp.setBatchNo(c.getBatchNo());
            return resp;
        }).collect(Collectors.toList());
    }
}
