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
    CkProductBomService productBomService;
    @Resource
    CkProductBomDetailService productBomDetailService;
    @Resource
    CkProductService productService;
    @Resource
    CkUnitService unitService;
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

    public Boolean importProduct(MultipartFile file, Long tenantId, Long userId) {

        List<Product> products = productService.listWareHouseEnable(tenantId);
        //把products处理成map, key是名称_规格_颜色, value是Product
        Map<String, Product> productNameSpecColor2ProductMap = products.stream().collect(Collectors.toMap(v -> v.getName() + "_" + v.getSpec() + "_" + v.getColor(), v -> v));


        // 1. 读取Excel
        List<ProductImportDto> plist = ExcelUtils.readExcel(file, ProductImportDto.class);

        List<ProductImportDto> productList = plist.subList(2, plist.size());
        List<Unit> units = unitService.selectByTenantId(tenantId, 1);
        Map<String, Unit> unitName2UnitMap = units.stream().collect(Collectors.toMap(Unit::getUnitName, v -> v));

        List<Product> batchSaveProductList = new ArrayList<>();
        List<InboundOrder> batchSaveInboundList = new ArrayList<>();

        for (ProductImportDto pi : productList) {
            Product p = new Product();
            if(productNameSpecColor2ProductMap.containsKey(pi.getName() + "_" + pi.getSpec() + "_" + pi.getColor())) {
                //入库数据

            } else {
                p.setSku(generateSmartSku(pi.getName(), pi.getColor(), pi.getSpec()));
                p.setTenantId(tenantId);
                //p.setBarcode();
                p.setName(pi.getName());
                p.setSpec(pi.getSpec());
                //p.setCategoryCode();
                p.setUnitCode(unitName2UnitMap.get("件").getUnitCode());
                p.setOutUnitCode(unitName2UnitMap.get("箱").getUnitCode());
                p.setOutUnitPerNum(StringUtils.isNotBlank(pi.getQuantityPerBox()) ?new BigDecimal(pi.getQuantityPerBox()):null);
                p.setWeightPerUnit(StringUtils.isNotBlank(pi.getWeightPerUnit()) ?new BigDecimal(pi.getWeightPerUnit()):null);
                p.setColor(pi.getColor());
                p.setMinStock(100L);
                p.setRemark(pi.getRemark());
                p.setStatus(1);
                p.setOutUnitHeight(StringUtils.isNotBlank(pi.getBoxHeight()) ?new BigDecimal(pi.getBoxHeight()):null);
                p.setOutUnitLength(StringUtils.isNotBlank(pi.getBoxLength()) ?new BigDecimal(pi.getBoxLength()):null);
                p.setOutUnitWidth(StringUtils.isNotBlank(pi.getBoxWidth()) ?new BigDecimal(pi.getBoxWidth()):null);
                batchSaveProductList.add(p);
            }

        }

        return true;
    }

    private void validateData(List<ProductImportDto> list) {
        // 校验SKU重复、分类是否存在、单位是否合法等
    }


//    private Product convertToEntity(ProductImportDto dto) {
//        Product entity = new Product();
//        entity.setTenantId(CurrentUser.getTenantId());
//        entity.setSku(dto.getSku());
//        entity.setName(dto.getName());
//        entity.setSpec(dto.getSpec());
//        entity.setCategoryCode(categoryMapper.findCodeByName(dto.getCategoryName()));
//        entity.setUnitCode(unitMapper.findCodeByName(dto.getUnitName()));
//        entity.setOutUnitCode(unitMapper.findCodeByName(dto.getOutUnitName()));
//        entity.setOutUnitPerNum(dto.getOutUnitPerNum());
//        entity.setWeightPerUnit(dto.getWeightPerUnit());
//        entity.setColor(dto.getColor());
//        entity.setMinStock(dto.getMinStock());
//        entity.setRemark(dto.getRemark());
//        entity.setStatus(1);
//        entity.setCreatedBy(CurrentUser.getUserId());
//        return entity;
//    }
}
