package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.dto.ProductCategory;
import com.example.entity.cangku.dto.Unit;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.ProductCategoryResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.UnitResp;
import com.example.service.CkProductCategoryService;
import com.example.service.CkProductService;
import com.example.service.CkUnitService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:10
 */
@Service
public class CKProductFacade {

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
        if (req.getParentCode() != null) {
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
        product.setModifiedAt(new Date());
        product.setModifiedBy(req.getUserId());
        product.setCreatedAt(new Date());
        product.setCreatedBy(req.getUserId());
        return productService.save(product);
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
        List<String> unitCodeList1 = list.getRecords().stream().map(v -> v.getUnitCode()).collect(Collectors.toList());
        List<String> unitCodeList2 = list.getRecords().stream().map(v -> v.getOutUnitCode()).collect(Collectors.toList());
        unitCodeList1.addAll(unitCodeList2);
        List<Unit> units = unitService.selectByTenantIdAndCodes(req.getTenantId(), unitCodeList1);
        Map<String, Unit> unitMap = units.stream().collect(Collectors.toMap(Unit::getUnitCode, v -> v));


        List<ProductPageListResp> collect = list.getRecords().stream().map(v -> {
            ProductPageListResp p = new ProductPageListResp();
            BeanUtils.copyProperties(v, p);

            p.setCategoryName(whMap.get(v.getCategoryCode()).getCategoryName());
            p.setUnitName(unitMap.get(v.getUnitCode()).getUnitName());
            p.setOutUnitName(unitMap.get(v.getOutUnitCode()).getUnitName());
            return p;
        }).collect(Collectors.toList());

        Page<ProductPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean update(ProductCreateReq req) {
        Product p = productService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("商品不存在");
        }

        Product save= new Product();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return productService.updateById(save);
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
}
