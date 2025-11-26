package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.req.excel.ProductBomExcelImportModel;
import com.example.entity.cangku.req.excel.ProductCreateImportModel;
import com.example.entity.cangku.resp.*;
import com.example.service.*;
import com.example.utils.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.utils.SmartSkuGenerator.generateSmartSku;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:10
 */
@Slf4j
@Service
public class CKProductFacade {

    @Resource
    CkInventoryService inventoryService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
    @Resource
    CkInventoryShelfService inventoryShelfService;
    @Resource
    CkInventoryWarehouseService inventoryWarehouseService;
    @Resource
    CkInventoryTransactionService inventoryTransactionService;
    @Resource
    CkInboundOrderService inboundOrderService;
    @Resource
    CkInboundOrderItemService inboundOrderItemService;
    @Resource
    CkOutboundOrderService outboundOrderService;
    @Resource
    CkOutboundOrderItemService outboundOrderItemService;
    @Resource
    CkShelfService shelfService;
    @Resource
    CkProductBomService productBomService;
    @Resource
    CkProductBomDetailService productBomDetailService;
    @Resource
    CkProductService productService;
    @Resource
    CkUnitService unitService;
    @Resource
    CkWareHouseService warehouseService;
    @Resource
    CkCustomerService customerService;
    @Resource
    CkSupplierService supplierService;
    @Resource
    private CkProductCategoryService productCategoryService;

    public List<ProductCategoryResp> categoryList(Long tenantId) {
        List<ProductCategory> list = productCategoryService.selectByTenantId(tenantId);
        return list.stream().map(item -> {
                    ProductCategoryResp resp = new ProductCategoryResp();
                    BeanUtils.copyProperties(item, resp);
                    return resp;
                }).sorted((o1, o2) -> o2.getSortOrder() - o1.getSortOrder())
                .collect(Collectors.toList());
    }

//    public List<ProductCategoryResp> categoryList(Long tenantId) {
//        List<ProductCategoryResp> result = new ArrayList<>();
//
//        // 查询出所有分类
//        List<ProductCategory> productCategories = productCategoryService.selectByTenantId(tenantId);
//        if (CollectionUtils.isEmpty(productCategories)) {
//            return result;
//        }
//
//        // 1. 先把所有分类转成 Resp 对象
//        Map<String, ProductCategoryResp> map = new HashMap<>();
//        for (ProductCategory category : productCategories) {
//            ProductCategoryResp resp = new ProductCategoryResp();
//            BeanUtils.copyProperties(category, resp);
//            resp.setChildren(new ArrayList<>());
//            map.put(category.getCategoryCode(), resp);
//        }
//
//        // 2. 构建父子关系
//        for (ProductCategoryResp resp : map.values()) {
//            // 找到 parent
//            if (StringUtils.isNotBlank(resp.getParentCode()) && map.containsKey(resp.getParentCode())) {
//                ProductCategoryResp parent = map.get(resp.getParentCode());
//                parent.getChildren().add(resp);
//            } else {
//                // 没有父级的就是根节点
//                result.add(resp);
//            }
//        }
//
//        // 3. 按 sortOrder 排序（可选）
//        sortRecursively(result);
//
//        return result;
//    }
//
//    /**
//     * 递归对子节点进行排序
//     */
//    private void sortRecursively(List<ProductCategoryResp> list) {
//        if (CollectionUtils.isEmpty(list)) {
//            return;
//        }
//        list.sort(Comparator.comparing(ProductCategoryResp::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
//        for (ProductCategoryResp resp : list) {
//            sortRecursively(resp.getChildren());
//        }
//    }

    public List<UnitResp> unitList(Long tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        //查询启用的单位
        List<Unit> list = unitService.selectByTenantId(tenantId, 1);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(v -> {
            UnitResp resp = new UnitResp();
            BeanUtils.copyProperties(v, resp);
            return resp;
        }).collect(Collectors.toList());
    }

    public Boolean categoryCreate(CategoryCreateReq req) {
        if (req == null) {
            return false;
        }
        ProductCategory save = new ProductCategory();
        if (StringUtils.isNotBlank(req.getParentCode())) {
            ProductCategory pc = productCategoryService.selectByTenantIdAndCode(req.getTenantId(), req.getParentCode());
            if (pc == null) {
                throw new ValidationException("父级分类不存在");
            } else {
                save.setParentCode(req.getParentCode());
                save.setLevel(pc.getLevel() + 1);
            }
        } else {
            save.setParentCode("0");
            save.setLevel(1);
        }
        save.setSortOrder(req.getSortOrder());
        save.setCategoryCode(req.getCategoryCode());
        save.setCategoryName(req.getCategoryName());
        save.setStatus(req.getStatus());
        save.setTenantId(req.getTenantId());


        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        save.setCreatedAt(new Date());
        save.setCreatedBy(req.getUserId());

        return productCategoryService.save(save);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean productCreate(ProductCreateReq req) {
        if (req == null || req.getTenantId() == null) {
            throw new ValidationException("参数错误");
        }

        Product product = productService.selectByTenantId(req.getTenantId(), req.getSku());
        if (product != null) {
            throw new ValidationException("商品sku已存在");
        }
        product = new Product();
        product.setTenantId(req.getTenantId());
        product.setSku(req.getSku());
        product.setBarcode(req.getBarcode());
        product.setName(req.getName());
        product.setEnglishName(req.getEnglishName());
        product.setSpec(req.getSpec());
        product.setCategoryCode(req.getCategoryCode());
        product.setUnitCode(req.getUnitCode());
        product.setOutUnitCode(req.getOutUnitCode());
        product.setOutUnitPerNum(req.getOutUnitPerNum());
        product.setWeightPerUnit(req.getWeightPerUnit());
        product.setColor(req.getColor());
        product.setMinStock(req.getMinStock());
        product.setRemark(req.getRemark());
        product.setStatus(req.getStatus());
        product.setOutUnitHeight(req.getOutUnitHeight());
        product.setOutUnitLength(req.getOutUnitLength());
        product.setOutUnitWidth(req.getOutUnitWidth());
        product.setModifiedAt(new Date());
        product.setModifiedBy(req.getUserId());
        product.setCreatedAt(new Date());
        product.setCreatedBy(req.getUserId());
        boolean save = productService.save(product);
        if (!save) {
            throw new ValidationException("保存商品失败");
        }
        if (Objects.nonNull(req.getBomData())) {
            ProductBomReq bomReq = req.getBomData();
            ProductBom bom = new ProductBom();
            bom.setProductId(product.getId());
            bom.setTenantId(req.getTenantId());
            bom.setBomCode(bomReq.getBomCode());
            bom.setVersion(bomReq.getVersion());
            bom.setStatus(bomReq.getStatus());
            bom.setRemark(bomReq.getRemark());
            bom.setCreatedAt(new Date());
            bom.setCreatedBy(req.getUserId());
            bom.setModifiedAt(new Date());
            bom.setModifiedBy(req.getUserId());
            boolean saveBom = productBomService.save(bom);
            if (!saveBom) {
                throw new ValidationException("保存配件失败");
            }
            List<ProductBomDetail> bomDetailList = bomReq.getDetails().stream().map(v -> {
                ProductBomDetail detail = new ProductBomDetail();
                detail.setBomId(bom.getId());
                detail.setTenantId(req.getTenantId());
                detail.setComponentProductId(v.getComponentProductId());
                detail.setQuantity(new BigDecimal(v.getQuantity()));
                detail.setLossRate(new BigDecimal(v.getLossRate()));
                detail.setRemark(v.getRemark());
                detail.setSortOrder(v.getSortOrder());
                detail.setCreatedAt(new Date());
                detail.setCreatedBy(req.getUserId());
                detail.setModifiedAt(new Date());
                detail.setModifiedBy(req.getUserId());
                return detail;
            }).collect(Collectors.toList());

            boolean saveBomDetail = productBomDetailService.saveBatch(bomDetailList);
            if (!saveBomDetail) {
                throw new ValidationException("保存配件明细失败");
            }
        }
        return true;
    }

    public Page<ProductPageListResp> pageList(Page<Product> page, ProductListPageReq req) {
        Page<Product> list = productService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<String> categoryCodes = list.getRecords().stream().map(v -> v.getCategoryCode()).collect(Collectors.toList());
        List<ProductCategory> categories = productCategoryService.selectByTenantIdAndCodes(req.getTenantId(), categoryCodes);
        Map<String, ProductCategory> whMap = categories.stream().collect(Collectors.toMap(ProductCategory::getCategoryCode, v -> v));


        //获取list.getRecords() 数据的unitCode和OutUnitCOde放入一个集合中
        List<String> allUnitCodes = list.getRecords().stream()
                .flatMap(v -> Stream.of(v.getUnitCode(), v.getOutUnitCode()))
                .filter(Objects::nonNull)   // 避免空值
                .distinct()                  // 去重
                .collect(Collectors.toList());

        List<Unit> units = unitService.selectByTenantIdAndCodes(req.getTenantId(), allUnitCodes);

        Map<String, Unit> unitMap = units.stream()
                .collect(Collectors.toMap(Unit::getUnitCode, Function.identity()));


        List<Long> productIds = list.getRecords().stream().map(v -> v.getId()).collect(Collectors.toList());
        List<ProductBom> boms = productBomService.selectByBomIds(productIds, req.getTenantId());
        Map<Long, ProductBom> bomId2BomMap = new HashMap<>();
        Map<Long, List<ProductBomDetail>> bomId2BomDetailListMap = new HashMap<>();
        Map<Long, Product> productId2ProductMap = new HashMap<>();
        if (!boms.isEmpty()) {
            bomId2BomMap = boms.stream().collect(Collectors.toMap(ProductBom::getProductId, v -> v));
            List<Long> bomIds = boms.stream().map(v -> v.getId()).collect(Collectors.toList());
            List<ProductBomDetail> bomDetails = productBomDetailService.selectByBomIds(bomIds, req.getTenantId());
            bomId2BomDetailListMap = bomDetails.stream().collect(Collectors.groupingBy(ProductBomDetail::getBomId));

            List<Long> compontProductIds = bomDetails.stream().map(v -> v.getComponentProductId()).distinct().collect(Collectors.toList());
            List<Product> products = productService.selectByIds(req.getTenantId(), compontProductIds);
            productId2ProductMap = products.stream().collect(Collectors.toMap(Product::getId, v -> v));
        }


        Map<Long, ProductBom> finalBomId2BomMap = bomId2BomMap;
        Map<Long, List<ProductBomDetail>> finalBomId2BomDetailListMap = bomId2BomDetailListMap;
        Map<Long, Product> finalProductId2ProductMap = productId2ProductMap;
        List<ProductPageListResp> collect = list.getRecords().stream().map(v -> {
            ProductPageListResp p = new ProductPageListResp();
            BeanUtils.copyProperties(v, p);

            ProductBom bom = finalBomId2BomMap.getOrDefault(v.getId(), new ProductBom());
            List<ProductBomDetail> bomdetailList = finalBomId2BomDetailListMap.getOrDefault(bom.getId(), new ArrayList<>());

            List<BomDetailListResp> detail = bomdetailList.stream().map(z -> {
                BomDetailListResp d = new BomDetailListResp();
                d.setComponentProductId(z.getComponentProductId());
                d.setComponentProductName(finalProductId2ProductMap.get(z.getComponentProductId()).getName());
                d.setComponentProductSku(finalProductId2ProductMap.get(z.getComponentProductId()).getSku());
                d.setComponentProductSpec(finalProductId2ProductMap.get(z.getComponentProductId()).getSpec());
                d.setComponentProductUnit(finalProductId2ProductMap.get(z.getComponentProductId()).getUnitCode());
                d.setQuantity(z.getQuantity());
                d.setLossRate(z.getLossRate());
                d.setRemark(z.getRemark());
                d.setSortOrder(z.getSortOrder());
                return d;
            }).sorted(Comparator.comparingInt(BomDetailListResp::getSortOrder)).collect(Collectors.toList());

            p.setCategoryName(whMap.get(v.getCategoryCode()).getCategoryName());
            p.setUnitName(unitMap.getOrDefault(v.getUnitCode(), new Unit()).getUnitName());
            p.setOutUnitName(unitMap.getOrDefault(v.getOutUnitCode(), new Unit()).getUnitName());
            p.setBomData(detail);
            return p;
        }).collect(Collectors.toList());

        Page<ProductPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ProductCreateReq req) {
        Product p = productService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("商品不存在");
        }

        Product save = new Product();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        boolean updateP = productService.updateById(save);
        if (!updateP) {
            throw new ValidationException("更新商品失败");
        }
        if (Objects.nonNull(req.getBomData())) {
            ProductBom pb = productBomService.selectByProduectId(save.getId(), req.getTenantId());
            if (!Objects.isNull(pb)) {
                Boolean deltedbyproductid = productBomService.deltedbyproductid(save.getId(), req.getUserId(), req.getTenantId());
                if (!deltedbyproductid) {
                    throw new ValidationException("删除商品BOM失败");
                }
                Boolean deletedByProdectId = productBomDetailService.deletedByBomId(pb.getId(), req.getUserId(), req.getTenantId());
                if (!deletedByProdectId) {
                    throw new ValidationException("删除商品BOM明细失败");
                }

            }


            ProductBomReq bomReq = req.getBomData();
            ProductBom bom = new ProductBom();
            bom.setProductId(save.getId());
            bom.setTenantId(req.getTenantId());
            bom.setBomCode(bomReq.getBomCode());
            bom.setVersion(bomReq.getVersion());
            bom.setStatus(bomReq.getStatus());
            bom.setRemark(bomReq.getRemark());
            bom.setCreatedAt(new Date());
            bom.setCreatedBy(req.getUserId());
            bom.setModifiedAt(new Date());
            bom.setModifiedBy(req.getUserId());
            boolean saveBom = productBomService.save(bom);
            if (!saveBom) {
                throw new ValidationException("保存配件失败");
            }
            List<ProductBomDetail> bomDetailList = bomReq.getDetails().stream().map(v -> {
                ProductBomDetail detail = new ProductBomDetail();
                detail.setBomId(bom.getId());
                detail.setTenantId(req.getTenantId());
                detail.setComponentProductId(v.getComponentProductId());
                detail.setQuantity(new BigDecimal(v.getQuantity()));
                detail.setLossRate(new BigDecimal(v.getLossRate()));
                detail.setRemark(v.getRemark());
                detail.setSortOrder(v.getSortOrder());
                detail.setCreatedAt(new Date());
                detail.setCreatedBy(req.getUserId());
                detail.setModifiedAt(new Date());
                detail.setModifiedBy(req.getUserId());
                return detail;
            }).collect(Collectors.toList());

            boolean saveBomDetail = productBomDetailService.saveBatch(bomDetailList);
            if (!saveBomDetail) {
                throw new ValidationException("保存配件明细失败");
            }
        }
        return true;
    }

    public Boolean updateStatus(ProductUpdateStatusReq req) {
        Product p = productService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("商品不存在");
        }

        Product save = new Product();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return productService.updateById(save);
    }

    public Boolean delete(ProductDeleteReq req) {
        Product p = productService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("商品不存在");
        }

        Product save = new Product();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return productService.updateById(save);
    }

    public ProductPageListResp detail(Long productId, UserInfo user) {
        Product p = productService.selectById(user.getTenantId(), productId);
        if (p == null) {
            throw new ValidationException("商品不存在");
        }

        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(user.getTenantId(), 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        ProductPageListResp result = new ProductPageListResp();
        BeanUtils.copyProperties(p, result);
        result.setUnitName(unitCode2UnitMap.getOrDefault(p.getUnitCode(), new Unit()).getUnitName());
        result.setOutUnitName(unitCode2UnitMap.getOrDefault(p.getOutUnitCode(), new Unit()).getUnitName());
        return result;
    }

    public List<ProductPageListResp> listEnable(UserInfo user) {
        List<Product> list = productService.listWareHouseEnable(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Product> productId2ProductMap = list.stream().collect(Collectors.toMap(Product::getId, v -> v));

        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(user.getTenantId(), 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        Map<Long, Long> productId2BomIdMap = new HashMap<>();
        List<ProductBom> boms = productBomService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(boms)) {
            productId2BomIdMap = boms.stream().collect(Collectors.toMap(ProductBom::getProductId, v -> v.getId()));
        }

        Map<Long, List<ProductBomDetail>> bomId2SubProductBomDetailMap = new HashMap<>();
        List<ProductBomDetail> bomDetails = productBomDetailService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(bomDetails)) {
            bomId2SubProductBomDetailMap = bomDetails.stream().collect(Collectors.groupingBy(ProductBomDetail::getBomId));
        }

        Map<Long, Inventory> productId2InventoryMap = new HashMap<>();
        List<Inventory> inventories = inventoryService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(inventories)) {
            productId2InventoryMap = inventories.stream().collect(Collectors.toMap(Inventory::getProductId, v -> v));
        }

        Map<Long, List<InventoryWarehouse>> productId2InventoryWarehouseMap = new HashMap<>();
        List<InventoryWarehouse> inventoryWarehouses = inventoryWarehouseService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(inventoryWarehouses)) {
            productId2InventoryWarehouseMap = inventoryWarehouses.stream().collect(Collectors.groupingBy(InventoryWarehouse::getProductId));
        }

        Map<Long, List<InventoryShelf>> productId2InventoryShelfMap = new HashMap<>();
        List<InventoryShelf> inventoryShelves = inventoryShelfService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(inventoryShelves)) {
            productId2InventoryShelfMap = inventoryShelves.stream().collect(Collectors.groupingBy(InventoryShelf::getProductId));
        }

        Map<Long, Warehouse> warehouseId2WarehouseMap = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouseId2WarehouseMap = warehouses.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));
        }

        Map<Long, WarehouseShelf> shelfId2ShelfMap = new HashMap<>();
        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(user.getTenantId());
        if (!CollectionUtils.isEmpty(warehouseShelves)) {
            shelfId2ShelfMap = warehouseShelves.stream().collect(Collectors.toMap(WarehouseShelf::getId, v -> v));
        }

        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        Map<Long, Long> finalProductId2BomIdMap = productId2BomIdMap;
        Map<Long, List<ProductBomDetail>> finalBomId2SubProductBomDetailMap = bomId2SubProductBomDetailMap;
        Map<Long, List<InventoryWarehouse>> finalProductId2InventoryWarehouseMap = productId2InventoryWarehouseMap;
        Map<Long, Warehouse> finalWarehouseId2WarehouseMap = warehouseId2WarehouseMap;
        Map<Long, List<InventoryShelf>> finalProductId2InventoryShelfMap = productId2InventoryShelfMap;
        Map<Long, WarehouseShelf> finalShelfId2ShelfMap = shelfId2ShelfMap;
        return list.stream().map(v -> {
            ProductPageListResp p = new ProductPageListResp();
            BeanUtils.copyProperties(v, p);
            p.setUnitName(finalUnitCode2UnitMap.getOrDefault(v.getUnitCode(), new Unit()).getUnitName());
            p.setOutUnitName(finalUnitCode2UnitMap.getOrDefault(v.getOutUnitCode(), new Unit()).getUnitName());
            Long productId = v.getId();
            if (finalProductId2BomIdMap.containsKey(productId)) {
                Long bomId = finalProductId2BomIdMap.get(productId);
                List<ProductBomDetail> subProductDetails = finalBomId2SubProductBomDetailMap.get(bomId);
                List<BomDetailListResp> bomData = subProductDetails.stream().map(s -> {
                    BomDetailListResp r = new BomDetailListResp();
                    r.setId(s.getId());
                    r.setComponentProductId(s.getComponentProductId());
                    r.setComponentProductName(productId2ProductMap.getOrDefault(s.getComponentProductId(), new Product()).getName());
                    r.setComponentProductSku(productId2ProductMap.getOrDefault(s.getComponentProductId(), new Product()).getSku());
                    r.setComponentProductUnit(finalUnitCode2UnitMap.getOrDefault(productId2ProductMap.getOrDefault(s.getComponentProductId(), new Product()).getUnitCode(), new Unit()).getUnitName());
                    r.setComponentProductSpec(productId2ProductMap.getOrDefault(s.getComponentProductId(), new Product()).getSpec());
                    r.setQuantity(s.getQuantity());
                    r.setLossRate(s.getLossRate());
                    r.setRemark(s.getRemark());
                    r.setSortOrder(s.getSortOrder());


                    List<InventoryWarehouse> inventoryWarehouseList = finalProductId2InventoryWarehouseMap.getOrDefault(s.getComponentProductId(), null);
                    if (!CollectionUtils.isEmpty(inventoryWarehouseList)) {
                        List<ProductWarehouseQuantityResp> productWarehouseQuantityRespStream = inventoryWarehouseList.stream().map(inventoryWarehouse -> {
                            ProductWarehouseQuantityResp pwqr = new ProductWarehouseQuantityResp();
                            pwqr.setWarehouseId(inventoryWarehouse.getWarehouseId());
                            pwqr.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(inventoryWarehouse.getWarehouseId(), new Warehouse()).getName());
                            pwqr.setWarehouseQuantity(inventoryWarehouse.getQuantity());
                            pwqr.setWarehouseAvailableQuantity(inventoryWarehouse.getQuantity().subtract(inventoryWarehouse.getLockedQuantity()));

                            List<InventoryShelf> inventoryShelfList = finalProductId2InventoryShelfMap.getOrDefault(s.getComponentProductId(), null);
                            if (!CollectionUtils.isEmpty(inventoryShelfList)) {
                                List<ProductShelfQuantityResp> psqrList = inventoryShelfList.stream()
                                        .filter(inventoryShelf -> inventoryShelf.getWarehouseId().equals(inventoryWarehouse.getWarehouseId()))
                                        .map(inventoryShelf -> {
                                            ProductShelfQuantityResp psqr = new ProductShelfQuantityResp();
                                            psqr.setShelfId(inventoryShelf.getShelfId());
                                            psqr.setShelfName(finalShelfId2ShelfMap.getOrDefault(inventoryShelf.getShelfId(), new WarehouseShelf()).getShelfName());
                                            psqr.setShelfQuantity(inventoryShelf.getQuantity());
                                            psqr.setShelfAvailableQuantity(inventoryShelf.getQuantity().subtract(inventoryShelf.getLockedQuantity()));
                                            return psqr;
                                        }).collect(Collectors.toList());
                                pwqr.setShelfQuantityList(psqrList);
                            }
                            return pwqr;
                        }).collect(Collectors.toList());
                        r.setWarehouseQuantityList(productWarehouseQuantityRespStream);
                    }
                    return r;
                }).collect(Collectors.toList());
                p.setBomData(bomData);
            } else {
                //没有bom
                Product pp = productId2ProductMap.getOrDefault(productId, new Product());
                BomDetailListResp r = new BomDetailListResp();
                r.setId(pp.getId());
                r.setComponentProductId(pp.getId());
                r.setComponentProductName(pp.getName());
                r.setComponentProductSku(pp.getSku());
                r.setComponentProductUnit(finalUnitCode2UnitMap.getOrDefault(pp.getUnitCode(), new Unit()).getUnitName());
                r.setComponentProductSpec(pp.getSpec());
                r.setQuantity(new BigDecimal(1));
                r.setLossRate(new BigDecimal(1));
                r.setRemark(pp.getRemark());
                r.setSortOrder(0);

                List<InventoryWarehouse> inventoryWarehouseList = finalProductId2InventoryWarehouseMap.getOrDefault(pp.getId(), null);
                if (!CollectionUtils.isEmpty(inventoryWarehouseList)) {
                    List<ProductWarehouseQuantityResp> productWarehouseQuantityRespStream = inventoryWarehouseList.stream().map(inventoryWarehouse -> {
                        ProductWarehouseQuantityResp pwqr = new ProductWarehouseQuantityResp();
                        pwqr.setWarehouseId(inventoryWarehouse.getWarehouseId());
                        pwqr.setWarehouseName(finalWarehouseId2WarehouseMap.getOrDefault(inventoryWarehouse.getWarehouseId(), new Warehouse()).getName());
                        pwqr.setWarehouseQuantity(inventoryWarehouse.getQuantity());
                        pwqr.setWarehouseAvailableQuantity(inventoryWarehouse.getQuantity().subtract(inventoryWarehouse.getLockedQuantity()));

                        List<InventoryShelf> inventoryShelfList = finalProductId2InventoryShelfMap.getOrDefault(pp.getId(), null);
                        if (!CollectionUtils.isEmpty(inventoryShelfList)) {
                            List<ProductShelfQuantityResp> psqrList = inventoryShelfList.stream()
                                    .filter(inventoryShelf -> inventoryShelf.getWarehouseId().equals(inventoryWarehouse.getWarehouseId()))
                                    .map(inventoryShelf -> {
                                        ProductShelfQuantityResp psqr = new ProductShelfQuantityResp();
                                        psqr.setShelfId(inventoryShelf.getShelfId());
                                        psqr.setShelfName(finalShelfId2ShelfMap.getOrDefault(inventoryShelf.getShelfId(), new WarehouseShelf()).getShelfName());
                                        psqr.setShelfQuantity(inventoryShelf.getQuantity());
                                        psqr.setShelfAvailableQuantity(inventoryShelf.getQuantity().subtract(inventoryShelf.getLockedQuantity()));
                                        return psqr;
                                    }).collect(Collectors.toList());
                            pwqr.setShelfQuantityList(psqrList);
                        }
                        return pwqr;
                    }).collect(Collectors.toList());
                    r.setWarehouseQuantityList(productWarehouseQuantityRespStream);
                }
                p.setBomData(Collections.singletonList(r));
            }
            return p;
        }).collect(Collectors.toList());
    }

    public List<BomDetailListResp> bomDetail(Long bomId, Long tenantId) {
        if (bomId == null || tenantId == null) {
            throw new ValidationException("参数错误");
        }
        ProductBom bom = productBomService.selectByBomId(bomId, tenantId);
        if (bom == null) {
            throw new ValidationException("BOM不存在");
        }

        List<ProductBomDetail> details = productBomDetailService.selectByBomId(bomId, tenantId);
        if (details.isEmpty()) {
            return new ArrayList<>();
        }

        return details.stream().map(v -> {
            BomDetailListResp r = new BomDetailListResp();
            BeanUtils.copyProperties(v, r);
            return r;
        }).collect(Collectors.toList());
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean importProduct(MultipartFile file, Long tenantId, Long userId) {
        try {
            // 1. 读取Excel数据
            List<ProductImportDto> importDataList = ExcelUtils.readExcel(file, ProductImportDto.class);
            if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 3) {
                throw new ValidationException("Excel文件数据不足");
            }

            // 2. 初始化基础数据（单位、供应商、客户、分类等）
            InitDataHolder initData = initializeBasicData(tenantId, userId, importDataList.get(1));

            // 3. 处理商品和货架数据
            List<ProductImportDto> productImportList = importDataList.subList(2, importDataList.size());
            processProductsAndShelves(tenantId, userId, productImportList, initData);

            // 4. 处理入库和出库业务
            processInventoryOperations(tenantId, userId, productImportList, initData);

            return true;
        } catch (Exception e) {
            log.error("导入商品数据失败: tenantId={}, userId={}", tenantId, userId, e);
            throw new ValidationException("导入失败: " + e.getMessage());
        }
    }

    /**
     * 初始化基础数据
     */
    private InitDataHolder initializeBasicData(Long tenantId, Long userId, ProductImportDto warehouseInfo) {
        InitDataHolder holder = new InitDataHolder();
        holder.warehouseInfo = warehouseInfo;

        // 初始化单位
        holder.unitOfPer = getOrCreateUnit(tenantId, userId, "件", "jian");
        holder.unitOfBox = getOrCreateUnit(tenantId, userId, "箱子", "box");

        // 初始化供应商、客户、分类
        holder.commonSupplier = getOrCreateSupplier(tenantId, userId, "通用供应商", "common");
        holder.commonCustomer = getOrCreateCustomer(tenantId, userId, "通用客户", "common");
        holder.commonCategory = getOrCreateCategory(tenantId, userId, "通用分类", "common");

        // 初始化仓库
        holder.warehouseName2WarehouseMap = initializeWarehouses(tenantId, userId, warehouseInfo);

        return holder;
    }

    /**
     * 获取或创建单位
     */
    private Unit getOrCreateUnit(Long tenantId, Long userId, String unitName, String unitCode) {
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        Unit unit = units.stream()
                .filter(v -> unitName.equals(v.getUnitName()))
                .findFirst()
                .orElse(null);

        if (unit == null) {
            unit = new Unit();
            unit.setTenantId(tenantId);
            unit.setUnitCode(unitCode);
            unit.setUnitName(unitName);
            unit.setStatus(1);
            unit.setRemark("系统默认生成");
            unit.setCreatedBy(userId);
            unit.setModifiedBy(userId);
            unit.setCreatedAt(new Date());
            unit.setModifiedAt(new Date());
            unitService.save(unit);
        }
        return unit;
    }

    /**
     * 获取或创建供应商
     */
    private Supplier getOrCreateSupplier(Long tenantId, Long userId, String supplierName, String supplierCode) {
        Supplier supplier = supplierService.selectByTenantIdAndSupplierName(tenantId, supplierName);
        if (supplier == null) {
            supplier = new Supplier();
            supplier.setTenantId(tenantId);
            supplier.setSupplierCode(supplierCode);
            supplier.setSupplierName(supplierName);
            supplier.setStatus(1);
            supplier.setRemark("系统默认生成");
            supplier.setCreatedBy(userId);
            supplier.setModifiedBy(userId);
            supplier.setCreatedAt(new Date());
            supplier.setModifiedAt(new Date());
            supplierService.save(supplier);
        }
        return supplier;
    }

    /**
     * 获取或创建客户
     */
    private Customer getOrCreateCustomer(Long tenantId, Long userId, String customerName, String customerCode) {
        Customer customer = customerService.selectByTenantIdAndCustomerName(tenantId, customerName);
        if (customer == null) {
            customer = new Customer();
            customer.setTenantId(tenantId);
            customer.setCustomerCode(customerCode);
            customer.setCustomerName(customerName);
            customer.setStatus(1);
            customer.setRemark("系统默认生成");
            customer.setCreatedBy(userId);
            customer.setModifiedBy(userId);
            customer.setCreatedAt(new Date());
            customer.setModifiedAt(new Date());
            customerService.save(customer);
        }
        return customer;
    }

    /**
     * 获取或创建分类
     */
    private ProductCategory getOrCreateCategory(Long tenantId, Long userId, String categoryName, String categoryCode) {
        List<ProductCategory> productCategories = productCategoryService.selectByTenantId(tenantId);
        ProductCategory category = productCategories.stream()
                .filter(v -> categoryName.equals(v.getCategoryName()))
                .findFirst()
                .orElse(null);

        if (category == null) {
            category = new ProductCategory();
            category.setTenantId(tenantId);
            category.setCategoryCode(categoryCode);
            category.setCategoryName(categoryName);
            category.setParentCode("");
            category.setLevel(1);
            category.setSortOrder(1);
            category.setStatus(1);
            category.setRemark("系统默认生成");
            category.setCreatedBy(userId);
            category.setModifiedBy(userId);
            category.setCreatedAt(new Date());
            category.setModifiedAt(new Date());
            productCategoryService.save(category);
        }
        return category;
    }

    /**
     * 初始化仓库
     */
    private Map<String, Warehouse> initializeWarehouses(Long tenantId, Long userId, ProductImportDto warehouseInfo) {
        List<Warehouse> existingWarehouses = warehouseService.selectByTenantId(tenantId);
        Map<String, Warehouse> warehouseMap = existingWarehouses.stream()
                .collect(Collectors.toMap(Warehouse::getName, v -> v));

        Set<String> warehouseNames = collectWarehouseNames(warehouseInfo);
        List<Warehouse> newWarehouses = new ArrayList<>();

        for (String warehouseName : warehouseNames) {
            if (StringUtils.isNotBlank(warehouseName) && !warehouseMap.containsKey(warehouseName)) {
                Warehouse warehouse = createWarehouse(tenantId, userId, warehouseName);
                newWarehouses.add(warehouse);
                warehouseMap.put(warehouseName, warehouse);
            }
        }

        if (!newWarehouses.isEmpty()) {
            warehouseService.saveBatch(newWarehouses);
        }

        return warehouseMap;
    }

    /**
     * 收集所有仓库名称
     */
    private Set<String> collectWarehouseNames(ProductImportDto warehouseInfo) {
        Set<String> warehouseNames = new HashSet<>();

        // 入库仓库
        addWarehouseName(warehouseNames, warehouseInfo.getInStockInfo1());
        addWarehouseName(warehouseNames, warehouseInfo.getInStockInfo2());
        addWarehouseName(warehouseNames, warehouseInfo.getInStockInfo3());
        addWarehouseName(warehouseNames, warehouseInfo.getInStockInfo4());

        // 出库仓库
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo1());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo2());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo3());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo4());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo5());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo6());
        addWarehouseName(warehouseNames, warehouseInfo.getOutStockInfo7());

        return warehouseNames;
    }

    private void addWarehouseName(Set<String> warehouseNames, String warehouseName) {
        if (StringUtils.isNotBlank(warehouseName)) {
            warehouseNames.add(warehouseName);
        }
    }

    /**
     * 创建仓库
     */
    private Warehouse createWarehouse(Long tenantId, Long userId, String warehouseName) {
        Warehouse warehouse = new Warehouse();
        warehouse.setTenantId(tenantId);
        warehouse.setName(warehouseName);
        warehouse.setCode(OrderNumberGenerator.generateWareCode());
        warehouse.setStatus(1);
        warehouse.setRemark("系统默认生成");
        warehouse.setCreatedBy(userId);
        warehouse.setModifiedBy(userId);
        warehouse.setCreatedAt(new Date());
        warehouse.setModifiedAt(new Date());
        return warehouse;
    }

    /**
     * 处理商品和货架数据
     */
    private void processProductsAndShelves(Long tenantId, Long userId, List<ProductImportDto> productImportList, InitDataHolder initData) {
        List<Product> existingProducts = productService.listWareHouseEnable(tenantId);
        Map<String, Product> productMap = existingProducts.stream()
                .collect(Collectors.toMap(
                        v -> buildProductKey(v.getName(), v.getSpec(), v.getColor()),
                        v -> v
                ));

        List<Product> newProducts = new ArrayList<>();
        List<WarehouseShelf> newShelves = new ArrayList<>();

        for (ProductImportDto importDto : productImportList) {
            if (StringUtils.isBlank(importDto.getName())) {
                continue;
            }

            String productKey = buildProductKey(importDto.getName(), importDto.getSpec(), importDto.getColor());
            if (!productMap.containsKey(productKey)) {
                // 创建新商品
                Product product = createProduct(tenantId, userId, importDto, initData);
                newProducts.add(product);
                productMap.put(productKey, product);

                // 创建货架
                createShelvesForProduct(tenantId, userId, importDto, initData, newShelves);
            }
        }

        // 批量保存
        if (!newProducts.isEmpty()) {
            List<Product> distinctProducts = newProducts.stream()
                    .collect(Collectors.toMap(
                            p -> buildProductKey(p.getName(), p.getSpec(), p.getColor()),
                            p -> p,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            productService.saveBatch(distinctProducts);
        }
        if (!newShelves.isEmpty()) {
            List<WarehouseShelf> distinctShelves = newShelves.stream()
                    .collect(Collectors.toMap(
                            s -> s.getShelfName() + "_" + s.getWarehouseId(),
                            s -> s,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            shelfService.saveBatch(distinctShelves);
        }
    }

    /**
     * 创建商品
     */
    private Product createProduct(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setSku(generateSmartSku(importDto.getName(), importDto.getColor(), importDto.getSpec()));
        product.setBarcode(importDto.getBarCode());
        product.setName(importDto.getName());
        product.setSpec(importDto.getSpec());
        product.setCategoryCode(initData.commonCategory.getCategoryCode());
        product.setUnitCode(initData.unitOfPer.getUnitCode());
        product.setOutUnitCode(initData.unitOfBox.getUnitCode());
        product.setOutUnitPerNum(getBigDecimal(importDto.getQuantityPerBox()));
        product.setWeightPerUnit(getBigDecimal(importDto.getWeightPerUnit()));
        product.setColor(importDto.getColor());
        product.setMinStock(100L);
        product.setRemark(importDto.getRemark());
        product.setStatus(1);
        product.setOutUnitHeight(getBigDecimal(importDto.getBoxHeight()));
        product.setOutUnitLength(getBigDecimal(importDto.getBoxLength()));
        product.setOutUnitWidth(getBigDecimal(importDto.getBoxWidth()));
        product.setCreatedBy(userId);
        product.setModifiedBy(userId);
        product.setCreatedAt(new Date());
        product.setModifiedAt(new Date());
        product.setIsDeleted(0);
        return product;
    }

    /**
     * 为商品创建货架
     */
    private void createShelvesForProduct(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData, List<WarehouseShelf> newShelves) {
        String shelfName = StringUtils.isNotBlank(importDto.getShelfName()) ? importDto.getShelfName() : "默认货架";

        // 收集所有相关的仓库ID
        Set<Long> warehouseIds = collectWarehouseIds(importDto, initData);

        for (Long warehouseId : warehouseIds) {
            WarehouseShelf shelf = new WarehouseShelf();
            shelf.setTenantId(tenantId);
            shelf.setShelfName(shelfName);
            shelf.setShelfCode(OrderNumberGenerator.generateShelfCode());
            shelf.setWarehouseId(warehouseId);
            shelf.setRemark("系统默认生成");
            shelf.setStatus(1);
            shelf.setCreatedBy(userId);
            shelf.setModifiedBy(userId);
            shelf.setCreatedAt(new Date());
            shelf.setModifiedAt(new Date());
            newShelves.add(shelf);
        }
    }

    /**
     * 收集仓库ID
     */
    private Set<Long> collectWarehouseIds(ProductImportDto importDto, InitDataHolder initData) {
        Set<Long> warehouseIds = new HashSet<>();
        ProductImportDto warehouseInfo = initData.warehouseInfo;
        // 入库仓库
        addWarehouseId(warehouseIds, warehouseInfo.getInStockInfo1(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getInStockInfo2(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getInStockInfo3(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getInStockInfo4(), initData);

        // 出库仓库
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo1(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo2(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo3(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo4(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo5(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo6(), initData);
        addWarehouseId(warehouseIds, warehouseInfo.getOutStockInfo7(), initData);

        return warehouseIds;
    }

    private void addWarehouseId(Set<Long> warehouseIds, String warehouseName, InitDataHolder initData) {
        if (StringUtils.isNotBlank(warehouseName)) {
            Warehouse warehouse = initData.warehouseName2WarehouseMap.get(warehouseName);
            if (warehouse != null) {
                warehouseIds.add(warehouse.getId());
            }
        }
    }

    /**
     * 处理库存操作（入库和出库）
     */
    private void processInventoryOperations(Long tenantId, Long userId, List<ProductImportDto> productImportList, InitDataHolder initData) {
        // 加载必要的数据
        List<Product> allProducts = productService.listWareHouseEnable(tenantId);
        Map<String, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(
                        v -> buildProductKey(v.getName(), v.getSpec(), v.getColor()),
                        v -> v
                ));

        List<WarehouseShelf> allShelves = shelfService.selectByTenantId(tenantId);
        Map<String, WarehouseShelf> shelfMap = allShelves.stream()
                .collect(Collectors.toMap(
                        v -> v.getShelfName() + "_" + v.getWarehouseId(),
                        v -> v
                ));

        // 处理每个商品的库存操作
        for (ProductImportDto importDto : productImportList) {
            if (StringUtils.isBlank(importDto.getName())) {
                continue;
            }

            String productKey = buildProductKey(importDto.getName(), importDto.getSpec(), importDto.getColor());
            Product product = productMap.get(productKey);
            if (product == null) {
                log.warn("商品不存在: {}", productKey);
                continue;
            }

            String batchNo = OrderNumberGenerator.generateOrderPC();

            // 处理入库操作
            processInboundOperations(tenantId, userId, importDto, initData, product, shelfMap, batchNo);

            // 处理出库操作
            processOutboundOperations(tenantId, userId, importDto, initData, product, shelfMap, batchNo);
        }
    }

    /**
     * 处理入库操作
     */
    private void processInboundOperations(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData,
                                          Product product, Map<String, WarehouseShelf> shelfMap, String batchNo) {
        processInboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getInStockInfo1(), initData.warehouseInfo.getInStockInfo1());
        processInboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getInStockInfo2(), initData.warehouseInfo.getInStockInfo2());
        processInboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getInStockInfo3(), initData.warehouseInfo.getInStockInfo3());
        processInboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getInStockInfo4(), initData.warehouseInfo.getInStockInfo4());
    }

    /**
     * 处理单个入库操作
     */
    private void processInboundOperation(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData,
                                         Product product, Map<String, WarehouseShelf> shelfMap, String batchNo,
                                         String quantityStr, String warehouseName) {
        if (StringUtils.isBlank(quantityStr) || StringUtils.isBlank(warehouseName)) {
            return;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityStr);
            BigDecimal unitPrice = MoneyUtils.cleanCurrencyString(importDto.getUnitPriceRmb());

            Warehouse warehouse = initData.warehouseName2WarehouseMap.get(warehouseName);
            if (warehouse == null) {
                log.warn("仓库不存在: {}", warehouseName);
                return;
            }

            // 创建入库单
            InboundOrder inboundOrder = createInboundOrder(tenantId, warehouse.getId(), initData.commonSupplier.getId(), quantity, unitPrice, importDto.getRemark());
            inboundOrderService.save(inboundOrder);

            // 创建入库单明细
            Long shelfLocationId = getShelfLocationId(importDto.getShelfName(), warehouse.getId(), shelfMap);
            InboundOrderItem item = createInboundOrderItem(tenantId, inboundOrder.getId(), product.getId(), quantity, shelfLocationId, batchNo, unitPrice, importDto.getRemark());
            inboundOrderItemService.save(item);

            // 更新库存
            updateInventoryForInbound(tenantId, userId, product.getId(), warehouse.getId(), quantity, batchNo, inboundOrder.getId(), item.getId(), unitPrice,shelfLocationId);

        } catch (Exception e) {
            log.error("处理入库操作失败: product={}, warehouse={}", product.getName(), warehouseName, e);
        }
    }

    /**
     * 处理出库操作
     */
    private void processOutboundOperations(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData,
                                           Product product, Map<String, WarehouseShelf> shelfMap, String batchNo) {
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo1(), initData.warehouseInfo.getOutStockInfo1());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo2(), initData.warehouseInfo.getOutStockInfo2());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo3(), initData.warehouseInfo.getOutStockInfo3());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo4(), initData.warehouseInfo.getOutStockInfo4());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo5(), initData.warehouseInfo.getOutStockInfo5());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo6(), initData.warehouseInfo.getOutStockInfo6());
        processOutboundOperation(tenantId, userId, importDto, initData, product, shelfMap, batchNo,
                importDto.getOutStockInfo7(), initData.warehouseInfo.getOutStockInfo7());
    }

    /**
     * 处理单个出库操作
     */
    private void processOutboundOperation(Long tenantId, Long userId, ProductImportDto importDto, InitDataHolder initData,
                                          Product product, Map<String, WarehouseShelf> shelfMap, String batchNo,
                                          String quantityStr, String warehouseName) {
        if (StringUtils.isBlank(quantityStr) || StringUtils.isBlank(warehouseName)) {
            return;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityStr);
            BigDecimal unitPrice = MoneyUtils.cleanCurrencyString(importDto.getUnitPriceRmb());

            Warehouse warehouse = initData.warehouseName2WarehouseMap.get(warehouseName);
            if (warehouse == null) {
                log.warn("仓库不存在: {}", warehouseName);
                return;
            }

            // 创建出库单
            OutboundOrder outboundOrder = createOutboundOrder(tenantId, warehouse.getId(), initData.commonCustomer.getId(), quantity, unitPrice, importDto.getRemark());
            outboundOrderService.save(outboundOrder);

            // 创建出库单明细
            OutboundOrderItem item = createOutboundOrderItem(tenantId, outboundOrder.getId(), product.getId(), quantity, batchNo, unitPrice, importDto.getRemark());
            outboundOrderItemService.save(item);

            // 更新库存
            updateInventoryForOutbound(tenantId, userId, product.getId(), warehouse.getId(), quantity, batchNo, outboundOrder.getId(), item.getId(), unitPrice);

        } catch (Exception e) {
            log.error("处理出库操作失败: product={}, warehouse={}", product.getName(), warehouseName, e);
        }
    }

// ==================== 库存更新相关方法 ====================

    /**
     * 更新入库库存
     */
    private void updateInventoryForInbound(Long tenantId, Long userId, Long productId, Long warehouseId,
                                           BigDecimal quantity, String batchNo, Long orderId, Long itemId, BigDecimal unitPrice, Long shelfLocationId) {

        BigDecimal oldQuantity = BigDecimal.ZERO;
        // 1. 更新库存表
        Inventory inventory = inventoryService.getByProduct(productId, tenantId);
        if (inventory != null) {
            oldQuantity = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity().add(quantity));
            inventory.setModifiedBy(userId);
            inventory.setModifiedAt(new Date());
            inventoryService.updateById(inventory);
        } else {
            inventory = new Inventory();
            inventory.setTenantId(tenantId);
            inventory.setProductId(productId);
            inventory.setQuantity(quantity);
            inventory.setLockedQuantity(BigDecimal.ZERO);
            inventory.setCreatedBy(userId);
            inventory.setModifiedBy(userId);
            inventory.setCreatedAt(new Date());
            inventory.setModifiedAt(new Date());
            inventoryService.save(inventory);
        }

        // 2. 更新库存批次表
        InventoryBatch inventoryBatch = inventoryBatchService.selectByProductId(productId, tenantId);
        if (inventoryBatch != null) {
            inventoryBatch.setQuantity(inventoryBatch.getQuantity().add(quantity));
            inventoryBatch.setModifiedBy(userId);
            inventoryBatch.setModifiedAt(new Date());
            inventoryBatchService.updateById(inventoryBatch);
        } else {
            inventoryBatch = new InventoryBatch();
            inventoryBatch.setTenantId(tenantId);
            inventoryBatch.setProductId(productId);
            inventoryBatch.setWarehouseId(warehouseId);
            inventoryBatch.setBatchNo(batchNo);
            inventoryBatch.setQuantity(quantity);
            inventoryBatch.setLockedQuantity(BigDecimal.ZERO);
            inventoryBatch.setInboundItemId(itemId);
            inventoryBatch.setInboundOrderId(orderId);
            inventoryBatch.setProductionDate(new Date());
            inventoryBatch.setCreatedBy(userId);
            inventoryBatch.setModifiedBy(userId);
            inventoryBatch.setCreatedAt(new Date());
            inventoryBatch.setModifiedAt(new Date());
            inventoryBatchService.save(inventoryBatch);
        }

        // 更新仓库货架库存表
        InventoryShelf inventoryShelf = inventoryShelfService.getByWarehouseAndProductAndShelf(warehouseId, productId, shelfLocationId, batchNo, tenantId);
        if (inventoryShelf != null) {
            inventoryShelf.setQuantity(inventoryShelf.getQuantity().add(quantity));
            inventoryShelf.setModifiedBy(userId);
            inventoryShelf.setModifiedAt(new Date());
            inventoryShelfService.updateById(inventoryShelf);
        } else {
            inventoryShelf = new InventoryShelf();
            inventoryShelf.setTenantId(tenantId);
            inventoryShelf.setProductId(productId);
            inventoryShelf.setBatchNo(batchNo);
            inventoryShelf.setWarehouseId(warehouseId);
            inventoryShelf.setShelfId(shelfLocationId);
            inventoryShelf.setQuantity(quantity);
            inventoryShelf.setLockedQuantity(BigDecimal.ZERO);
            inventoryShelf.setCreatedBy(userId);
            inventoryShelf.setModifiedBy(userId);
            inventoryShelf.setCreatedAt(new Date());
            inventoryShelf.setModifiedAt(new Date());
            inventoryShelfService.save(inventoryShelf);
        }

        // 3. 更新仓库库存表
        InventoryWarehouse inventoryWarehouse = inventoryWarehouseService.getByWarehouseAndProduct(warehouseId, productId, tenantId);
        if (inventoryWarehouse != null) {
            inventoryWarehouse.setQuantity(inventoryWarehouse.getQuantity().add(quantity));
            inventoryWarehouse.setModifiedBy(userId);
            inventoryWarehouse.setModifiedAt(new Date());
            inventoryWarehouseService.updateById(inventoryWarehouse);
        } else {
            inventoryWarehouse = new InventoryWarehouse();
            inventoryWarehouse.setTenantId(tenantId);
            inventoryWarehouse.setWarehouseId(warehouseId);
            inventoryWarehouse.setProductId(productId);
            inventoryWarehouse.setQuantity(quantity);
            inventoryWarehouse.setLockedQuantity(BigDecimal.ZERO);
            inventoryWarehouse.setCreatedBy(userId);
            inventoryWarehouse.setModifiedBy(userId);
            inventoryWarehouse.setCreatedAt(new Date());
            inventoryWarehouse.setModifiedAt(new Date());
            inventoryWarehouseService.save(inventoryWarehouse);
        }

        // 4. 创建库存流水记录
        InventoryTransaction inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setTenantId(tenantId);
        inventoryTransaction.setWarehouseId(warehouseId);
        inventoryTransaction.setProductId(productId);
        inventoryTransaction.setOrderType(1); // 1表示入库
        inventoryTransaction.setOrderId(orderId);
        inventoryTransaction.setOrderItemId(itemId);
        inventoryTransaction.setChangeQuantity(quantity);
        inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
        inventoryTransaction.setBeforBalanceQuantity(oldQuantity);
        inventoryTransaction.setTransactionTime(new Date());
        inventoryTransaction.setPriceUnit(unitPrice);
        inventoryTransaction.setPriceTotal(quantity.multiply(unitPrice));
        inventoryTransaction.setCreatedBy(userId);
        inventoryTransaction.setModifiedBy(userId);
        inventoryTransaction.setCreatedAt(new Date());
        inventoryTransaction.setModifiedAt(new Date());
        inventoryTransactionService.save(inventoryTransaction);
    }

    /**
     * 更新出库库存
     */
    private void updateInventoryForOutbound(Long tenantId, Long userId, Long productId, Long warehouseId,
                                            BigDecimal quantity, String batchNo, Long orderId, Long itemId, BigDecimal unitPrice) {

        BigDecimal oldInventoryQuantity = BigDecimal.ZERO;
        // 1. 更新库存表
        Inventory inventory = inventoryService.getByProduct(productId, tenantId);
        if (inventory != null) {
            oldInventoryQuantity = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity().subtract(quantity));
            inventory.setModifiedBy(userId);
            inventory.setModifiedAt(new Date());
            inventoryService.updateById(inventory);
        } else {
            inventory = new Inventory();
            inventory.setTenantId(tenantId);
            inventory.setProductId(productId);
            inventory.setQuantity(quantity.negate()); // 出库时库存为负值
            inventory.setLockedQuantity(BigDecimal.ZERO);
            inventory.setCreatedBy(userId);
            inventory.setModifiedBy(userId);
            inventory.setCreatedAt(new Date());
            inventory.setModifiedAt(new Date());
            inventoryService.save(inventory);
        }

        // 2. 更新库存批次表
        InventoryBatch inventoryBatch = inventoryBatchService.selectByProductId(productId, tenantId);
        if (inventoryBatch != null) {
            inventoryBatch.setQuantity(inventoryBatch.getQuantity().subtract(quantity));
            inventoryBatch.setModifiedBy(userId);
            inventoryBatch.setModifiedAt(new Date());
            inventoryBatchService.updateById(inventoryBatch);
        } else {
            inventoryBatch = new InventoryBatch();
            inventoryBatch.setTenantId(tenantId);
            inventoryBatch.setProductId(productId);
            inventoryBatch.setWarehouseId(warehouseId);
            inventoryBatch.setBatchNo(batchNo);
            inventoryBatch.setQuantity(quantity.negate()); // 出库时批次库存为负值
            inventoryBatch.setLockedQuantity(BigDecimal.ZERO);
            inventoryBatch.setInboundItemId(itemId);
            inventoryBatch.setInboundOrderId(orderId);
            inventoryBatch.setProductionDate(new Date());
            inventoryBatch.setCreatedBy(userId);
            inventoryBatch.setModifiedBy(userId);
            inventoryBatch.setCreatedAt(new Date());
            inventoryBatch.setModifiedAt(new Date());
            inventoryBatchService.save(inventoryBatch);
        }

        // 3. 更新仓库库存表
        InventoryWarehouse inventoryWarehouse = inventoryWarehouseService.getByWarehouseAndProduct(warehouseId, productId, tenantId);
        if (inventoryWarehouse != null) {
            inventoryWarehouse.setQuantity(inventoryWarehouse.getQuantity().subtract(quantity));
            inventoryWarehouse.setModifiedBy(userId);
            inventoryWarehouse.setModifiedAt(new Date());
            inventoryWarehouseService.updateById(inventoryWarehouse);
        } else {
            inventoryWarehouse = new InventoryWarehouse();
            inventoryWarehouse.setTenantId(tenantId);
            inventoryWarehouse.setWarehouseId(warehouseId);
            inventoryWarehouse.setProductId(productId);
            inventoryWarehouse.setQuantity(quantity.negate()); // 出库时仓库库存为负值
            inventoryWarehouse.setLockedQuantity(BigDecimal.ZERO);
            inventoryWarehouse.setCreatedBy(userId);
            inventoryWarehouse.setModifiedBy(userId);
            inventoryWarehouse.setCreatedAt(new Date());
            inventoryWarehouse.setModifiedAt(new Date());
            inventoryWarehouseService.save(inventoryWarehouse);
        }

        // 4. 创建库存流水记录
        InventoryTransaction inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setTenantId(tenantId);
        inventoryTransaction.setWarehouseId(warehouseId);
        inventoryTransaction.setProductId(productId);
        inventoryTransaction.setOrderType(2); // 2表示出库
        inventoryTransaction.setOrderId(orderId);
        inventoryTransaction.setOrderItemId(itemId);
        inventoryTransaction.setChangeQuantity(quantity.negate()); // 出库数量为负
        inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
        inventoryTransaction.setBeforBalanceQuantity(oldInventoryQuantity);
        inventoryTransaction.setTransactionTime(new Date());
        inventoryTransaction.setPriceUnit(unitPrice);
        inventoryTransaction.setPriceTotal(quantity.multiply(unitPrice));
        inventoryTransaction.setCreatedBy(userId);
        inventoryTransaction.setModifiedBy(userId);
        inventoryTransaction.setCreatedAt(new Date());
        inventoryTransaction.setModifiedAt(new Date());
        inventoryTransactionService.save(inventoryTransaction);
    }

// ==================== 辅助方法 ====================

    private InboundOrder createInboundOrder(Long tenantId, Long warehouseId, Long supplierId, BigDecimal quantity, BigDecimal unitPrice, String remark) {
        InboundOrder order = new InboundOrder();
        order.setTenantId(tenantId);
        order.setOrderNo(OrderNumberGenerator.generateOrderNo());
        order.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
        order.setWarehouseId(warehouseId);
        order.setSupplierId(supplierId);
        order.setOrderType(2);
        order.setStatus(3);
        order.setTotalQuantity(quantity);
        order.setTotalAmount(quantity.multiply(unitPrice));
        order.setRemark(remark);
        return order;
    }

    private InboundOrderItem createInboundOrderItem(Long tenantId, Long orderId, Long productId, BigDecimal quantity, Long shelfLocationId, String batchNo, BigDecimal unitPrice, String remark) {
        InboundOrderItem item = new InboundOrderItem();
        item.setTenantId(tenantId);
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setActualQuantity(quantity);
        item.setShelfLocationId(shelfLocationId);
        item.setBatchNo(batchNo);
        item.setRemark(remark);
        item.setPriceUnit(unitPrice);
        item.setPriceTotal(quantity.multiply(unitPrice));
        return item;
    }

    private OutboundOrder createOutboundOrder(Long tenantId, Long warehouseId, Long customerId, BigDecimal quantity, BigDecimal unitPrice, String remark) {
        OutboundOrder order = new OutboundOrder();
        order.setTenantId(tenantId);
        order.setOrderNo(OrderNumberGenerator.generateOrderNo());
        order.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
        order.setWarehouseId(warehouseId);
        order.setCustomerId(customerId);
        order.setOrderType(2);
        order.setStatus(3);
        order.setTotalQuantity(quantity);
        order.setTotalAmount(quantity.multiply(unitPrice));
        order.setRemark(remark);
        return order;
    }

    private OutboundOrderItem createOutboundOrderItem(Long tenantId, Long orderId, Long productId, BigDecimal quantity, String batchNo, BigDecimal unitPrice, String remark) {
        OutboundOrderItem item = new OutboundOrderItem();
        item.setTenantId(tenantId);
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setBatchNo(batchNo);
        item.setRemark(remark);
        item.setPriceUnit(unitPrice);
        item.setPriceTotal(quantity.multiply(unitPrice));
        return item;
    }

    private Long getShelfLocationId(String shelfName, Long warehouseId, Map<String, WarehouseShelf> shelfMap) {
        if (StringUtils.isNotBlank(shelfName)) {
            String shelfKey = shelfName + "_" + warehouseId;
            WarehouseShelf shelf = shelfMap.get(shelfKey);
            return shelf != null ? shelf.getId() : null;
        }
        return null;
    }

    private BigDecimal getBigDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("数值格式错误: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private String buildProductKey(String name, String spec, String color) {
        return name + "_" + spec + "_" + color;
    }

    public List<ProductSimpleListResp> productSimpleList(UserInfo user,String keyword) {
        if (StringUtils.isEmpty( keyword)) {
            return Collections.emptyList();
        }
        List<Product> products = productService.listWareHouseEnable(user.getTenantId());
        return products.stream()
                .filter(product -> product.getName().contains(keyword))
                .map(product -> {
                    ProductSimpleListResp resp = new ProductSimpleListResp();
                    BeanUtils.copyProperties(product, resp);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean importBomExcel(MultipartFile file, Long tenantId, Long userId, Long productId) {
        Product product = productService.selectById(tenantId, productId);
        if (product == null) {
            throw new ValidationException("成品不存在");
        }
        // 1. 读取Excel数据
        List<ProductBomExcelImportModel> importDataList = ExcelUtils.readExcel(file, ProductBomExcelImportModel.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }
        //获取或者保存分类
        ProductCategory commonBom = productCategoryService.selectByTenantIdAndCode(tenantId, "common_bom");
        if (commonBom == null) {
            commonBom = new ProductCategory();
            commonBom.setTenantId(tenantId);
            commonBom.setCategoryCode("common_bom");
            commonBom.setCategoryName("通用原料分类");
            commonBom.setCreatedAt(new Date());
            commonBom.setCreatedBy(userId);
            commonBom.setModifiedAt(new Date());
            commonBom.setModifiedBy(userId);
            productCategoryService.save(commonBom);
        }

        Map<String, Unit> unitName2UnitBeforSaveMap = new HashMap<>();
        List<Unit> unitsBeforSave = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty( unitsBeforSave)) {
            unitName2UnitBeforSaveMap = unitsBeforSave.stream().collect(Collectors.toMap(Unit::getUnitName, v -> v));
        }

        //先保存单位
        Map<String, Unit> finalUnitName2UnitMap1 = unitName2UnitBeforSaveMap;
        List<String> unitNameNeedCreate = importDataList.stream()
                .filter(v-> !finalUnitName2UnitMap1.containsKey(v.getUnitName()))
                .map(v -> v.getUnitName()).distinct().collect(Collectors.toList());

        List<Unit> unitCreateList = unitNameNeedCreate.stream().map(v -> {
            Unit unit = new Unit();
            unit.setTenantId(tenantId);
            unit.setUnitCode(ChineseUtils.chineseToPinyin(v));
            unit.setUnitName(v);
            unit.setCreatedAt(new Date());
            unit.setCreatedBy(userId);
            unit.setModifiedAt(new Date());
            unit.setModifiedBy(userId);
            return unit;
        }).collect(Collectors.toList());
        unitService.saveBatch(unitCreateList);

        List<Unit> unitsAfterCreate = unitService.selectByTenantId(tenantId, 1);
        Map<String, Unit> unitName2UnitMap = new HashMap<>();
        if (!CollectionUtils.isEmpty( unitsAfterCreate)) {
            unitName2UnitMap = unitsAfterCreate.stream().collect(Collectors.toMap(Unit::getUnitName, v -> v));
        }


        //先保存不存在的原料
        List<Product> products = productService.listWareHouseEnable(tenantId);
        Map<String, Product>  productNameSpecColorExist2InfoMap = products.stream().collect(Collectors.toMap(v -> buildProductKey(v.getName(), v.getSpec(), v.getColor()), v -> v));
        if (!CollectionUtils.isEmpty( products)) {
            productNameSpecColorExist2InfoMap = products.stream().collect(Collectors.toMap(v -> buildProductKey(v.getName(), v.getSpec(), v.getColor()), v -> v));
        }
        ProductCategory finalCommonBom = commonBom;
        Map<String, Unit> finalUnitName2UnitMap = unitName2UnitMap;
        List< Product> pbomList = new ArrayList<>();
        for (ProductBomExcelImportModel v : importDataList) {
            if (productNameSpecColorExist2InfoMap.containsKey(buildProductKey(v.getName(), v.getSpec(), v.getColor()))) {
                continue;
            }
            Product pBom = new Product();
            pBom.setTenantId(tenantId);
            pBom.setSku(StringUtils.isNotBlank(v.getSku()) ? v.getSku() : generateSmartSku(v.getName(), v.getColor(), v.getSpec()));
            pBom.setBarcode("barCode-" + generateSmartSku(v.getName(), v.getColor(), v.getSpec()));
            pBom.setName(v.getName());
            pBom.setSpec(v.getSpec());
            pBom.setColor(v.getColor());
            pBom.setCategoryCode(finalCommonBom.getCategoryCode());
            pBom.setUnitCode(finalUnitName2UnitMap.getOrDefault(v.getUnitName(), new Unit()).getUnitCode());
            pBom.setWeightPerUnit(StringUtils.isNotBlank(v.getWeightPerUnit()) ? new BigDecimal(v.getWeightPerUnit()) : BigDecimal.ZERO);
            pBom.setMinStock(StringUtils.isNotBlank(v.getMinStock()) ?
                    new BigDecimal(v.getMinStock()).longValue() : 100L);
            pBom.setRemark(v.getRemark());
            pBom.setStatus(1);
            pBom.setCreatedAt(new Date());
            pBom.setCreatedBy(userId);
            pBom.setModifiedAt(new Date());
            pBom.setModifiedBy(userId);
            pbomList.add(pBom);
        }
        productService.saveBatch(pbomList);

        //创建 bom 表数据
        ProductBom pb = new ProductBom();
        pb.setTenantId(tenantId);
        pb.setProductId(productId);
        pb.setBomCode(product.getSku() + "_BOM");
        pb.setVersion("V1.0");
        pb.setStatus(1);
        boolean save = productBomService.save(pb);
        if (!save) {
            throw new ValidationException("保存BOM数据失败");
        }

        List<Product> productsAfterCreate = productService.listWareHouseEnable(tenantId);
        Map<String, Product> productNameSpecColor2InfoMap = productsAfterCreate.stream().collect(Collectors.toMap(v -> buildProductKey(v.getName(), v.getSpec(), v.getColor()), v -> v));
        // 创建bomdetail 数据
        List<ProductBomDetail> collect = importDataList.stream().map(v->{
            ProductBomDetail detail = new ProductBomDetail();
            detail.setTenantId(tenantId);
            detail.setBomId(pb.getId());
            if (!productNameSpecColor2InfoMap.containsKey(buildProductKey(v.getName(), v.getSpec(), v.getColor()))){
                throw new ValidationException("未找到对应的原料");
            } else{
                detail.setComponentProductId(productNameSpecColor2InfoMap.get(buildProductKey(v.getName(), v.getSpec(), v.getColor())).getId());
            }
            if (StringUtils.isBlank(v.getQuantity())) {
                throw new ValidationException("数量不能为空");
            } else {
                detail.setQuantity(new BigDecimal(v.getQuantity()));
            }
            detail.setLossRate(StringUtils.isBlank(v.getLossRate()) ? BigDecimal.ZERO : new BigDecimal(NumUtils.parsePercentStrict(v.getLossRate())));
            detail.setRemark(v.getRemark());
            detail.setType(StringUtils.isBlank(v.getTypeName()) ? 2 : v.getTypeName().contains("辅") ? 2 : 1);
            detail.setCreatedAt(new Date());
            detail.setCreatedBy(userId);
            detail.setModifiedAt(new Date());
            detail.setModifiedBy(userId);
            return  detail;
        }).collect(Collectors.toList());
        
        return productBomDetailService.saveBatch(collect);
    }

    /**
     * 数据持有类，用于传递初始化数据
     */
    private static class InitDataHolder {
        Unit unitOfPer;
        Unit unitOfBox;
        Supplier commonSupplier;
        Customer commonCustomer;
        ProductCategory commonCategory;
        Map<String, Warehouse> warehouseName2WarehouseMap;
        ProductImportDto warehouseInfo;
    }

    public Boolean importOutboundSaleQuantity(MultipartFile file, Long tenantId, Long userId) {
        // 1. 读取Excel数据
        List<ProductCreateImportModel> importDataList = ExcelUtils.readExcel(file, ProductCreateImportModel.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }

        Map<String, ProductCategory> categoryName2CategoryMap = new HashMap<>();
        List<ProductCategory> categoryList = productCategoryService.selectByTenantId(tenantId);
        if (!CollectionUtils.isEmpty(categoryList)) {
            categoryName2CategoryMap = categoryList.stream().collect(Collectors.toMap(ProductCategory::getCategoryName, v -> v));
        }

        Map<String, Unit> unitName2UnitMap = new HashMap<>();
        List<Unit> unitList = unitService.selectByTenantId(tenantId, 1);
        if (!CollectionUtils.isEmpty(unitList)) {
            unitName2UnitMap = unitList.stream().collect(Collectors.toMap(Unit::getUnitName, v -> v));
        }


        Set<String> duplicateSkuSet = new HashSet<>();
        Set<String> duplicateNameSpecColorSet = new HashSet<>();
        Map<String, ProductCategory> finalCategoryName2CategoryMap = categoryName2CategoryMap;
        Map<String, Unit> finalUnitName2UnitMap = unitName2UnitMap;
        List<Product> saveList = importDataList.stream().map(v -> {
            if(StringUtils.isBlank(v.getSku())) {
                throw new ValidationException("SKU不能为空:" + v.getName());
            }
            if (duplicateSkuSet.contains(v.getSku())) {
                throw new ValidationException("SKU重复:" + v.getSku());
            }
            duplicateSkuSet.add(v.getSku());

            if (duplicateNameSpecColorSet.contains(buildProductKey(v.getName(), v.getSpec(), v.getColor()))) {
                throw new ValidationException("商品名称规格型号颜色重复:" + v.getName() + "_" + v.getSpec() + "_" + v.getColor());
            }
            duplicateNameSpecColorSet.add(buildProductKey(v.getName(), v.getSpec(), v.getColor()));



            Product r = new Product();
            r.setTenantId(tenantId);
            r.setSku(v.getSku());
            r.setBarcode("BarCode-" + v.getSku());
            r.setName(StringUtils.isNotBlank(v.getName()) ? v.getName() : v.getEnglishName());
            r.setSpec(v.getSpec());
            r.setCategoryCode(finalCategoryName2CategoryMap.getOrDefault("通用分类", new ProductCategory()).getCategoryCode());
            r.setUnitCode(finalUnitName2UnitMap.getOrDefault("件", new Unit()).getUnitCode());
            r.setOutUnitCode(finalUnitName2UnitMap.getOrDefault("箱", new Unit()).getUnitCode());
            r.setOutUnitPerNum(StringUtils.isNotBlank(v.getOutUnitPerNum()) ? new BigDecimal(v.getOutUnitPerNum()) : BigDecimal.ONE);
            r.setWeightPerUnit(StringUtils.isNotBlank(v.getWeightPerUnit()) ? new BigDecimal(v.getWeightPerUnit()) : BigDecimal.ONE);
            r.setColor(v.getColor());
            r.setMinStock(StringUtils.isNotBlank(v.getMinStock()) ? Long.valueOf(v.getMinStock()) : 100);
            r.setRemark(v.getRemark());
            r.setStatus(1);
            r.setEnglishName(v.getEnglishName());
            r.setOutUnitHeight(StringUtils.isNotBlank(v.getOutUnitHeight()) ? new BigDecimal(v.getOutUnitHeight()) : BigDecimal.ZERO);
            r.setOutUnitLength(StringUtils.isNotBlank(v.getOutUnitLength()) ? new BigDecimal(v.getOutUnitLength()) : BigDecimal.ZERO);
            r.setOutUnitWidth(StringUtils.isNotBlank(v.getOutUnitWidth()) ? new BigDecimal(v.getOutUnitWidth()) : BigDecimal.ZERO);
            return r;
        }).collect(Collectors.toList());

        return productService.saveBatch(saveList);
    }

}

