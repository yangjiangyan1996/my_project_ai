package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.*;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.BomDetailListResp;
import com.example.entity.cangku.resp.ProductCategoryResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.UnitResp;
import com.example.service.*;
import com.example.utils.ExcelUtils;
import com.example.utils.MoneyUtils;
import com.example.utils.OrderNumberGenerator;
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
@Service
public class CKProductFacade {

    @Resource
    CkInventoryService inventoryService;
    @Resource
    CkInventoryBatchService inventoryBatchService;
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
    CkWareHouseService wareHouseService;
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
            p.setUnitName(unitMap.get(v.getUnitCode()).getUnitName());
            p.setOutUnitName(unitMap.get(v.getOutUnitCode()).getUnitName());
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

    public List<ProductPageListResp> listEnable(UserInfo user) {
        List<Product> list = productService.listWareHouseEnable(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Unit> unitCode2UnitMap = new HashMap<>();
        List<Unit> units = unitService.selectByTenantId(user.getTenantId(), 1);
        if (!CollectionUtils.isEmpty(units)) {
            unitCode2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));
        }

        Map<String, Unit> finalUnitCode2UnitMap = unitCode2UnitMap;
        return list.stream().map(v -> {
            ProductPageListResp p = new ProductPageListResp();
            BeanUtils.copyProperties(v, p);
            p.setUnitName(finalUnitCode2UnitMap.get(v.getUnitCode()).getUnitName());
            p.setOutUnitName(finalUnitCode2UnitMap.get(v.getOutUnitCode()).getUnitName());
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

        List<Product> productsBeforInitList = productService.listWareHouseEnable(tenantId);
        //把products处理成map, key是名称_规格_颜色, value是Product


        // 1. 读取Excel
        List<ProductImportDto> plist = ExcelUtils.readExcel(file, ProductImportDto.class);

        //把products处理成map, key是名称_规格_颜色, value是Product
        Map<String, Product> productNameSpecColor2ProductBeforInitMap = productsBeforInitList.stream().collect(Collectors.toMap(v -> v.getName() + "_" + v.getSpec() + "_" + v.getColor(), v -> v));

        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        Unit  unitOfPer = units.stream().filter(v -> v.getUnitName().equals("件")).findFirst().orElse(null);
        Unit  unitOfBox = units.stream().filter(v -> v.getUnitName().equals("箱子")).findFirst().orElse(null);
        //如果不存在件，则创建件
        if (unitOfPer == null) {
            unitOfPer = new Unit();
            unitOfPer.setTenantId(tenantId);
            unitOfPer.setUnitCode("jian");
            unitOfPer.setUnitName("件");
            unitOfPer.setStatus("1");
            unitOfPer.setRemark("系统默认生成");
            unitOfPer.setCreatedBy(userId);
            unitOfPer.setModifiedBy(userId);
            unitOfPer.setCreatedAt(new Date());
            unitOfPer.setModifiedAt(new Date());
            unitService.save(unitOfPer);
        }
        if (unitOfBox == null) {
            unitOfBox = new Unit();
            unitOfBox.setTenantId(tenantId);
            unitOfBox.setUnitCode("box");
            unitOfBox.setUnitName("箱子");
            unitOfBox.setStatus("1");
            unitOfBox.setRemark("系统默认生成");
            unitOfBox.setCreatedBy(userId);
            unitOfBox.setCreatedAt(new Date());
            unitOfBox.setModifiedBy(userId);
            unitOfBox.setModifiedAt(new Date());
            unitService.save(unitOfBox);
        }
        //获取供应商，如果没有【通用供应商】，创建【通用供应商】，然后吧导入的数据，没有供应商的挂靠在这个供应商的ID下面
        Supplier commonSupplier = supplierService.selectByTenantIdAndSupplierName(tenantId, "通用供应商");
        if (commonSupplier == null) {
            commonSupplier = new Supplier();
            commonSupplier.setTenantId(tenantId);
            commonSupplier.setSupplierCode("common");
            commonSupplier.setSupplierName("通用供应商");
            commonSupplier.setStatus(1);
            commonSupplier.setRemark("系统默认生成");
            commonSupplier.setCreatedBy(userId);
            commonSupplier.setModifiedBy(userId);
            commonSupplier.setCreatedAt(new Date());
            commonSupplier.setModifiedAt(new Date());
            supplierService.save(commonSupplier);
        }

        //获取客户，如果没有【通用客户】，创建【通用客户】，然后吧导入的数据，没有客户的挂靠在这个客户的ID下面
        Customer commonCustomer = customerService.selectByTenantIdAndCustomerName(tenantId, "通用客户");
        if (commonCustomer == null) {
            commonCustomer = new Customer();
            commonCustomer.setTenantId(tenantId);
            commonCustomer.setCustomerCode("common");
            commonCustomer.setCustomerName("通用客户");
            commonCustomer.setStatus(1);
            commonCustomer.setRemark("系统默认生成");
            commonCustomer.setCreatedBy(userId);
            commonCustomer.setModifiedBy(userId);
            commonCustomer.setCreatedAt(new Date());
            commonCustomer.setModifiedAt(new Date());
            customerService.save(commonCustomer);
        }


        //获取分类，如果没有【通用分类】，创建【通用分类】，然后吧导入的数据，没有分类的挂靠在这个分类的ID下面
        List<ProductCategory> productCategories = productCategoryService.selectByTenantId(tenantId);
        ProductCategory commonCategory = productCategories.stream().filter(v -> v.getCategoryName().equals("通用分类")).findFirst().orElse(null);
        if (commonCategory == null) {
            commonCategory = new ProductCategory();
            commonCategory.setTenantId(tenantId);
            commonCategory.setCategoryCode("common");
            commonCategory.setCategoryName("通用分类");
            commonCategory.setParentCode("");
            commonCategory.setLevel(1);
            commonCategory.setSortOrder(1);
            commonCategory.setStatus(1);
            commonCategory.setRemark("系统默认生成");
            commonCategory.setCreatedBy(userId);
            commonCategory.setModifiedBy(userId);
            commonCategory.setCreatedAt(new Date());
            commonCategory.setModifiedAt(new Date());
            productCategoryService.save(commonCategory);
        }

        //查询仓库，如果不存在，需要新增
        List<Warehouse> warehouseListBeforeInitList = wareHouseService.selectByTenantId(tenantId);
        Map<String, Warehouse> warehouseName2WarehouseBeforeInitMap = warehouseListBeforeInitList.stream().collect(Collectors.toMap(Warehouse::getName, v -> v));

        ProductImportDto productImportDto = plist.get(1);
        List<Warehouse> batchInitWarehouseList = new ArrayList<>();
        Set<String> uniqueWarehouseNameSet = new HashSet<>();
        if (StringUtils.isNotBlank(productImportDto.getInStockInfo1()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getInStockInfo1())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getInStockInfo1())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getInStockInfo1());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getInStockInfo2()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getInStockInfo2())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getInStockInfo2())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getInStockInfo2());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getInStockInfo3()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getInStockInfo3())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getInStockInfo3())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getInStockInfo3());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getInStockInfo4()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getInStockInfo4())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getInStockInfo4())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getInStockInfo4());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        //出库的库名
        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo1()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo1())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo1())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo1());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo2()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo2())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo2())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo2());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo3()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo3())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo3())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo3());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo4()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo4())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo4())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo4());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }


        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo5()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo5())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo5())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo5());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo6()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo6())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo6())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo6());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        if (StringUtils.isNotBlank(productImportDto.getOutStockInfo7()) &&
                !warehouseName2WarehouseBeforeInitMap.containsKey(productImportDto.getOutStockInfo7())) {
            if (uniqueWarehouseNameSet.add(productImportDto.getOutStockInfo7())) {
                Warehouse warehouse = new Warehouse();
                warehouse.setTenantId(tenantId);
                warehouse.setName(productImportDto.getOutStockInfo7());
                warehouse.setCode(OrderNumberGenerator.generateWareCode());
                warehouse.setStatus(1);
                warehouse.setRemark("系统默认生成");
                warehouse.setCreatedBy(userId);
                warehouse.setModifiedBy(userId);
                warehouse.setCreatedAt(new Date());
                warehouse.setModifiedAt(new Date());
                batchInitWarehouseList.add(warehouse);
            }
        }

        wareHouseService.saveBatch(batchInitWarehouseList);






        List<ProductImportDto> productListOfImprot = plist.subList(2, plist.size());

        List<Warehouse> warehouseToInitShelfList = wareHouseService.selectByTenantId(tenantId);
        Map<String, Warehouse> warehouseName2WarehouseToInitShelfMap = warehouseToInitShelfList.stream().collect(Collectors.toMap(Warehouse::getName, v -> v));

        ProductImportDto wareNameUserOfImport = plist.get(1);


        //查询货架，如果不存在，需要新增
        List<WarehouseShelf> initShelfList = new ArrayList<>();

        //查询商品，如果不存在，需要新增
        List<Product> initProductList = new ArrayList<>();
        for (ProductImportDto im : productListOfImprot) {
            if (StringUtils.isBlank(im.getName())) {
                continue;
            }
            if (!productNameSpecColor2ProductBeforInitMap.containsKey(im.getName() + "_" + im.getSpec() + "_" + im.getColor())) {
                Product p = new Product();
                p.setTenantId(tenantId);
                p.setSku(generateSmartSku(im.getName(), im.getColor(), im.getSpec()));
                p.setBarcode(im.getBarCode());
                p.setName(im.getName());
                p.setSpec(im.getSpec());
                p.setCategoryCode(commonCategory.getCategoryCode());
                p.setUnitCode(unitOfPer.getUnitCode());
                p.setOutUnitCode(unitOfBox.getUnitCode());
                p.setOutUnitPerNum(im.getQuantityPerBox()==null?new BigDecimal(0):new BigDecimal(im.getQuantityPerBox()));
                p.setWeightPerUnit(im.getWeightPerUnit()==null?new BigDecimal(0):new BigDecimal(im.getWeightPerUnit()));
                p.setColor(im.getColor());
                p.setMinStock(100L);
                p.setRemark(im.getRemark());
                p.setStatus(1);
                p.setOutUnitHeight(im.getBoxHeight()==null?new BigDecimal(0):new BigDecimal(im.getBoxHeight()));
                p.setOutUnitLength(im.getBoxLength()==null?new BigDecimal(0):new BigDecimal(im.getBoxLength()));
                p.setOutUnitWidth(im.getBoxWidth()==null?new BigDecimal(0):new BigDecimal(im.getBoxWidth()));
                p.setCreatedBy(userId);
                p.setModifiedBy(userId);
                p.setCreatedAt(new Date());
                p.setModifiedAt(new Date());
                p.setIsDeleted(0);
                initProductList.add(p);


                //看货架在那个仓库有数据
                WarehouseShelf shelf = new WarehouseShelf();
                shelf.setTenantId(tenantId);
                shelf.setShelfName(StringUtils.isNotBlank(im.getShelfName())?im.getShelfName():"默认货架");
                shelf.setShelfCode(OrderNumberGenerator.generateShelfCode());
                shelf.setRemark("系统默认生成");
                shelf.setStatus(1);
                shelf.setCreatedBy(userId);
                shelf.setModifiedBy(userId);
                shelf.setCreatedAt(new Date());
                shelf.setModifiedAt(new Date());

                if (StringUtils.isNotBlank(im.getInStockInfo1())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getInStockInfo1()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getInStockInfo2())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getInStockInfo2()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getInStockInfo3())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getInStockInfo3()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getInStockInfo4())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getInStockInfo4()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo1())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo1()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo2())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo2()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo3())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo3()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo4())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo4()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo5())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo5()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo6())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo6()).getId());
                    initShelfList.add(shelf);
                }
                if (StringUtils.isNotBlank(im.getOutStockInfo7())) {
                    shelf.setWarehouseId(warehouseName2WarehouseToInitShelfMap.get(wareNameUserOfImport.getOutStockInfo7()).getId());
                    initShelfList.add(shelf);
                }
            }
        }

        List<Product> initProductListFilter = initProductList.stream()
                .collect(Collectors.toMap(
                        im -> im.getName() + "_" + im.getSpec() + "_" + im.getColor(),
                        v -> v,
                        (existing, replacement) -> existing  // 保留已存在的，忽略新的
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        productService.saveBatch(initProductListFilter);

        //initShelfList 再根据名称和仓库ID都相同时去重
        List<WarehouseShelf> list = initShelfList.stream()
                .collect(Collectors.toMap(
                        shelf -> shelf.getShelfName() + "_" + shelf.getWarehouseId(),
                        v -> v,
                        (existing, replacement) -> existing  // 保留已存在的，忽略新的
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        shelfService.saveBatch(list);

        //到这里数据已经准备就绪


        List<Warehouse> warehouseList = wareHouseService.selectByTenantId(tenantId);
        Map<String, Warehouse> warehouseName2WarehouseMap = warehouseList.stream().collect(Collectors.toMap(Warehouse::getName, v -> v));

        List<WarehouseShelf> warehouseShelves = shelfService.selectByTenantId(tenantId);
        Map<String, WarehouseShelf> warehouseShelfName_WareHourseId2WarehouseShelfMap = warehouseShelves.stream().collect(Collectors.toMap(v->v.getShelfName() + "_" + v.getWarehouseId(), v -> v));

        List<Product> productList = productService.listWareHouseEnable(tenantId);

        //Map<String, List<Product>> collect = productList.stream().collect(Collectors.groupingBy(v -> v.getName() + "_" + v.getSpec() + "_" + v.getColor()));


        Map<String, Product> productNameSpecColor2ProductMap = productList.stream().collect(Collectors.toMap(v -> v.getName() + "_" + v.getSpec() + "_" + v.getColor(), v -> v));


        for (ProductImportDto pi : productListOfImprot) {
            String batchNo = OrderNumberGenerator.generateOrderPC();
            //入库1
            if (StringUtils.isNotBlank(pi.getInStockInfo1())) {
                InboundOrder in = new InboundOrder();
                in.setTenantId(tenantId);
                in.setOrderNo(OrderNumberGenerator.generateOrderNo());
                in.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                in.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getInStockInfo1()).getId());
                in.setSupplierId(commonSupplier.getId());
                in.setOrderType(2);
                in.setStatus(3);
                in.setTotalQuantity(new BigDecimal(pi.getInStockInfo1()));
                in.setTotalAmount(new BigDecimal(pi.getInStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                in.setRemark(pi.getRemark());

                inboundOrderService.save(in);

                InboundOrderItem item = new InboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(in.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setActualQuantity(new BigDecimal(pi.getInStockInfo1()));
                if (StringUtils.isNotBlank(pi.getShelfName())) {
                    item.setShelfLocationId(warehouseShelfName_WareHourseId2WarehouseShelfMap.getOrDefault(pi.getShelfName() + "_" + in.getWarehouseId(), new WarehouseShelf()).getId());
                }
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getInStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                try{
                    inboundOrderItemService.save(item);
                }catch (Exception e) {
                    e.printStackTrace();
                }


                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().add(item.getActualQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getActualQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().add(item.getActualQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(in.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getActualQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(in.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(in.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().add(item.getActualQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(in.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getActualQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(in.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(1);
                inventoryTransaction.setOrderId(in.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getActualQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getInStockInfo1())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getInStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }

                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);

            }

            //入库2
            if (StringUtils.isNotBlank(pi.getInStockInfo2())) {
                InboundOrder in = new InboundOrder();
                in.setTenantId(tenantId);
                in.setOrderNo(OrderNumberGenerator.generateOrderNo());
                in.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                in.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getInStockInfo2()).getId());
                in.setSupplierId(commonSupplier.getId());
                in.setOrderType(2);
                in.setStatus(3);
                in.setTotalQuantity(new BigDecimal(pi.getInStockInfo2()));
                in.setTotalAmount(new BigDecimal(pi.getInStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                in.setRemark(pi.getRemark());

                inboundOrderService.save(in);

                InboundOrderItem item = new InboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(in.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setActualQuantity(new BigDecimal(pi.getInStockInfo2()));
                if (StringUtils.isNotBlank(pi.getShelfName())) {
                    item.setShelfLocationId(warehouseShelfName_WareHourseId2WarehouseShelfMap.getOrDefault(pi.getShelfName() + "_" + in.getWarehouseId(), new WarehouseShelf()).getId());
                }
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getInStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                inboundOrderItemService.save(item);


                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().add(item.getActualQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getActualQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().add(item.getActualQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(in.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getActualQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(in.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(in.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().add(item.getActualQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(in.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getActualQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(in.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(1);
                inventoryTransaction.setOrderId(in.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getActualQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getInStockInfo2())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getInStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);

            }

            //入库3
            if (StringUtils.isNotBlank(pi.getInStockInfo3())) {
                InboundOrder in = new InboundOrder();
                in.setTenantId(tenantId);
                in.setOrderNo(OrderNumberGenerator.generateOrderNo());
                in.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                in.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getInStockInfo3()).getId());
                in.setSupplierId(commonSupplier.getId());
                in.setOrderType(2);
                in.setStatus(3);
                in.setTotalQuantity(new BigDecimal(pi.getInStockInfo3()));
                in.setTotalAmount(new BigDecimal(pi.getInStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                in.setRemark(pi.getRemark());

                inboundOrderService.save(in);

                InboundOrderItem item = new InboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(in.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setActualQuantity(new BigDecimal(pi.getInStockInfo3()));
                if (StringUtils.isNotBlank(pi.getShelfName())) {
                    item.setShelfLocationId(warehouseShelfName_WareHourseId2WarehouseShelfMap.getOrDefault(pi.getShelfName() + "_" + in.getWarehouseId(), new WarehouseShelf()).getId());
                }
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getInStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                inboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().add(item.getActualQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getActualQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().add(item.getActualQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(in.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getActualQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(in.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(in.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().add(item.getActualQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(in.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getActualQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(in.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(1);
                inventoryTransaction.setOrderId(in.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getActualQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getInStockInfo3())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getInStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }

                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);

            }


            //入库4
            if (StringUtils.isNotBlank(pi.getInStockInfo4())) {
                InboundOrder in = new InboundOrder();
                in.setTenantId(tenantId);
                in.setOrderNo(OrderNumberGenerator.generateOrderNo());
                in.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                in.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getInStockInfo4()).getId());
                in.setSupplierId(commonSupplier.getId());
                in.setOrderType(2);
                in.setStatus(3);
                in.setTotalQuantity(new BigDecimal(pi.getInStockInfo4()));
                in.setTotalAmount(new BigDecimal(pi.getInStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                in.setRemark(pi.getRemark());

                inboundOrderService.save(in);

                InboundOrderItem item = new InboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(in.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setActualQuantity(new BigDecimal(pi.getInStockInfo4()));
                if (StringUtils.isNotBlank(pi.getShelfName())) {
                    item.setShelfLocationId(warehouseShelfName_WareHourseId2WarehouseShelfMap.getOrDefault(pi.getShelfName() + "_" + in.getWarehouseId(), new WarehouseShelf()).getId());
                }
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getInStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                inboundOrderItemService.save(item);


                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().add(item.getActualQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getActualQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().add(item.getActualQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(in.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getActualQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(in.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(in.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().add(item.getActualQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(in.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getActualQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(in.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(1);
                inventoryTransaction.setOrderId(in.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getActualQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getInStockInfo4())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getInStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);

            }


            //出库1
            if (StringUtils.isNotBlank(pi.getOutStockInfo1())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo1()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo1()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo1()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);


                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getOutStockInfo1())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo1()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);

            }

            //出库2
            if (StringUtils.isNotBlank(pi.getOutStockInfo2())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo2()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo2()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo2()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if(StringUtils.isNotBlank(pi.getOutStockInfo2())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo2()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }


            //出库3
            if (StringUtils.isNotBlank(pi.getOutStockInfo3())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo3()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo3()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo3()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getOutStockInfo3())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo3()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }


            //出库4
            if (StringUtils.isNotBlank(pi.getOutStockInfo4())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo4()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo4()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo4()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getOutStockInfo4())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo4()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }


            //出库5
            if (StringUtils.isNotBlank(pi.getOutStockInfo5())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo5()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo5()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo5()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo5()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo5()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getOutStockInfo5())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo5()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }


            //出库6
            if (StringUtils.isNotBlank(pi.getOutStockInfo6())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo6()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo6()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo6()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo6()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo6()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if (StringUtils.isNotBlank(pi.getOutStockInfo6())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo6()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }

            //出库7
            if (StringUtils.isNotBlank(pi.getOutStockInfo7())) {
                OutboundOrder out = new OutboundOrder();
                out.setTenantId(tenantId);
                out.setOrderNo(OrderNumberGenerator.generateOrderNo());
                out.setRelatedOrderNo(OrderNumberGenerator.generateRelatedOrderNo());
                out.setWarehouseId(warehouseName2WarehouseMap.get(wareNameUserOfImport.getOutStockInfo7()).getId());
                out.setCustomerId(commonCustomer.getId());
                out.setOrderType(2);
                out.setStatus(3);
                out.setTotalQuantity(new BigDecimal(pi.getOutStockInfo7()));
                out.setTotalAmount(new BigDecimal(pi.getOutStockInfo7()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                out.setRemark(pi.getRemark());

                outboundOrderService.save(out);

                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(tenantId);
                item.setOrderId(out.getId());
                item.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                item.setQuantity(new BigDecimal(pi.getOutStockInfo7()));
                item.setBatchNo(batchNo);
                item.setRemark(pi.getRemark());
                item.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                item.setPriceTotal(new BigDecimal(pi.getOutStockInfo7()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                outboundOrderItemService.save(item);

                //库存表
                Inventory inventory = inventoryService.getByProduct(item.getProductId(), tenantId);
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
                    inventory.setModifiedBy(userId);
                    inventory.setModifiedAt(new Date());
                    inventoryService.updateById(inventory);
                } else {
                    inventory = new Inventory();
                    inventory.setTenantId(tenantId);
                    inventory.setProductId(item.getProductId());
                    inventory.setQuantity(item.getQuantity());
                    inventory.setLockedQuantity(BigDecimal.ZERO);
                    inventory.setCreatedBy(userId);
                    inventory.setModifiedBy(userId);
                    inventory.setCreatedAt(new Date());
                    inventory.setModifiedAt(new Date());
                    inventoryService.save(inventory);
                }

                //库存批次表
                InventoryBatch ib = inventoryBatchService.selectByProductId(item.getProductId(), tenantId);
                if (ib != null) {
                    ib.setQuantity(ib.getQuantity().subtract(item.getQuantity()));
                    ib.setModifiedBy(userId);
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.updateById(ib);
                } else {
                    ib = new InventoryBatch();
                    ib.setTenantId(tenantId);
                    ib.setProductId(item.getProductId());
                    ib.setWarehouseId(out.getWarehouseId());
                    ib.setBatchNo(batchNo);
                    ib.setQuantity(item.getQuantity());
                    ib.setLockedQuantity(BigDecimal.ZERO);
                    ib.setInboundItemId(item.getId());
                    ib.setInboundOrderId(out.getId());
                    ib.setProductionDate(new Date());
                    ib.setCreatedBy(userId);
                    ib.setModifiedBy(userId);
                    ib.setCreatedAt(new Date());
                    ib.setModifiedAt(new Date());
                    inventoryBatchService.save(ib);
                }
                //仓库库存表
                InventoryWarehouse iw = inventoryWarehouseService.getByWarehouseAndProduct(out.getWarehouseId(),item.getProductId() , tenantId);
                if (iw != null) {
                    iw.setQuantity(iw.getQuantity().subtract(item.getQuantity()));
                    iw.setModifiedBy(userId);
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.updateById(iw);
                } else {
                    iw = new InventoryWarehouse();
                    iw.setTenantId(tenantId);
                    iw.setWarehouseId(out.getWarehouseId());
                    iw.setProductId(item.getProductId());
                    iw.setQuantity(item.getQuantity());
                    iw.setLockedQuantity(BigDecimal.ZERO);
                    iw.setCreatedBy(userId);
                    iw.setModifiedBy(userId);
                    iw.setCreatedAt(new Date());
                    iw.setModifiedAt(new Date());
                    inventoryWarehouseService.save(iw);
                }


                //库存流水表
                InventoryTransaction inventoryTransaction = new InventoryTransaction();
                inventoryTransaction.setTenantId(tenantId);
                inventoryTransaction.setWarehouseId(out.getWarehouseId());
                inventoryTransaction.setProductId(productNameSpecColor2ProductMap.getOrDefault(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor(),new Product()).getId());
                inventoryTransaction.setOrderType(2);
                inventoryTransaction.setOrderId(out.getId());
                inventoryTransaction.setOrderItemId(item.getId());
                inventoryTransaction.setChangeQuantity(item.getQuantity());
                inventoryTransaction.setBalanceQuantity(inventory.getQuantity());
                inventoryTransaction.setTransactionTime(new Date());
                inventoryTransaction.setPriceUnit(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb()));
                if(StringUtils.isNotBlank(pi.getOutStockInfo7())) {
                    inventoryTransaction.setPriceTotal(new BigDecimal(pi.getOutStockInfo7()).multiply(MoneyUtils.cleanCurrencyString(pi.getUnitPriceRmb())));
                }
                inventoryTransaction.setCreatedBy(userId);
                inventoryTransaction.setModifiedBy(userId);
                inventoryTransaction.setCreatedAt(new Date());
                inventoryTransaction.setModifiedAt(new Date());
                inventoryTransactionService.save(inventoryTransaction);
            }
        }

        return true;
    }

}
