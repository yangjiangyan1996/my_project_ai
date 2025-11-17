package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.dto.CustomerSkuMapping;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.req.SkuDeleteReq;
import com.example.entity.cangku.req.excel.SkuImportDto;
import com.example.entity.cangku.req.SkuListPageReq;
import com.example.entity.cangku.req.SkuUpdateStatusReq;
import com.example.entity.cangku.resp.SkuCreateReq;
import com.example.entity.cangku.resp.SkuPageListResp;
import com.example.entity.cangku.resp.excel.SkuExcelModel;
import com.example.service.CkCustomerService;
import com.example.service.CkCustomerSkuMappingService;
import com.example.service.CkProductService;
import com.example.utils.ExcelUtils;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:15
 */
@Service
@Slf4j
public class CkSkuFacade {
    @Resource
    CkCustomerService customerService;
    @Resource
    CkProductService productService;
    @Resource
    private CkCustomerSkuMappingService customerSkuMappingService;

    public Page<SkuPageListResp> pageList(Page<CustomerSkuMapping> page, SkuListPageReq req) {
        if (!StringUtils.isEmpty(req.getProductName())) {
            List<Product> products = productService.selectByProductNameLike(req.getTenantId(), req.getProductName());
            List<String> skuCodes = products.stream().map(v -> v.getSku()).distinct().collect(Collectors.toList());
            req.setProductSkus(skuCodes);
        }
        Page<CustomerSkuMapping> list = customerSkuMappingService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<String> skuList = list.getRecords().stream().map(v -> v.getProductSku()).distinct().collect(Collectors.toList());
        Map<String, Product> productSku2ProductMap = new HashMap<>();
        List<Product> products = productService.selectBySkus(req.getTenantId(), skuList);
        if (!CollectionUtils.isEmpty(products)) {
            productSku2ProductMap = products.stream().collect(Collectors.toMap(Product::getSku, v -> v));
        }


        List<Long> customerIds = list.getRecords().stream().map(v -> v.getCustomerId()).distinct().collect(Collectors.toList());
        List<Customer> customers = customerService.selectByTenantIdAndCustomerIds(req.getTenantId(), customerIds);
        Map<Long, Customer> customerId2CustomerMap = customers.stream().collect(Collectors.toMap(Customer::getId, v -> v));

        Map<String, Product> finalProductSku2ProductMap = productSku2ProductMap;
        List<SkuPageListResp> collect = list.getRecords().stream().map(v -> {
            SkuPageListResp p = new SkuPageListResp();
            BeanUtils.copyProperties(v, p);

            if (finalProductSku2ProductMap.containsKey(v.getProductSku())) {
                Product product = finalProductSku2ProductMap.get(v.getProductSku());
                p.setProductName(product.getName());
                p.setColor(product.getColor());
                p.setSpec(product.getSpec());
            }

            if (customerId2CustomerMap.containsKey(v.getCustomerId())) {
                Customer customer = customerId2CustomerMap.get(v.getCustomerId());
                p.setCustomerName(customer.getCustomerName());
            }
            return p;
        }).collect(Collectors.toList());

        Page<SkuPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean delete(SkuDeleteReq req) {
        CustomerSkuMapping p = customerSkuMappingService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("sku映射不存在");
        }

        CustomerSkuMapping save = new CustomerSkuMapping();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerSkuMappingService.updateById(save);
    }


    public Boolean importSku(MultipartFile file, Long tenantId, Long userId, Long customerId) {
        // 1. 读取Excel数据
        List<SkuImportDto> importDataList = ExcelUtils.readExcel(file, SkuImportDto.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }

        List<CustomerSkuMapping> customerSkuMappings = customerSkuMappingService.selectByCustomerId(customerId, tenantId);
        Map<String, CustomerSkuMapping> productSku2CustomerSkuMappingMap = customerSkuMappings.stream().collect(Collectors.toMap(CustomerSkuMapping::getProductSku, v -> v));

        List<SkuImportDto> productImportList = importDataList.subList(0, importDataList.size());

        List<CustomerSkuMapping> collect = productImportList.stream().map(v -> {
            if (productSku2CustomerSkuMappingMap.containsKey(v.getProductSku())) {
                //跳过
                return null;
            }
            CustomerSkuMapping s = new CustomerSkuMapping();
            s.setTenantId(tenantId);
            s.setProductSku(v.getProductSku());
            s.setCustomerSku(v.getCustomerSku());
            s.setCustomerId(customerId);
            s.setModifiedAt(new Date());
            s.setModifiedBy(userId);
            s.setCreatedAt(new Date());
            s.setCreatedBy(userId);
            s.setStatus(1);
            return s;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return customerSkuMappingService.saveBatch(collect);
    }

    public List<SkuExcelModel> getSkuExportData(Long tenantId) {
        if (tenantId == null) {
            throw new ValidationException("租户ID不能为空");
        }

        List<Product> products = productService.listWareHouseEnable(tenantId);
        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }
        return products.stream().map(v -> {
            SkuExcelModel model = new SkuExcelModel();
            model.setName(v.getName());
            model.setSpec(v.getSpec());
            model.setColor(v.getColor());
            model.setMySku(v.getSku());
            return model;
        }).collect(Collectors.toList());
    }

    public Boolean create(SkuCreateReq req) {
        if (req == null) {
            throw new ValidationException("参数错误");
        }

        CustomerSkuMapping s = customerSkuMappingService.selectByCustomerIdAndCustomerSku(req.getTenantId(), req.getProductSku(), req.getCustomerId(), req.getCustomerSku());
        if (s != null) {
            throw new ValidationException("映射已存在");
        }
        s = new CustomerSkuMapping();
        s.setTenantId(req.getTenantId());
        s.setProductSku(req.getProductSku());
        s.setCustomerId(req.getCustomerId());
        s.setCustomerSku(req.getCustomerSku());
        s.setStatus(req.getStatus());
        s.setRemark(req.getRemark());
        s.setModifiedAt(new Date());
        s.setModifiedBy(req.getUserId());
        s.setCreatedAt(new Date());
        s.setCreatedBy(req.getUserId());
        return customerSkuMappingService.save(s);
    }

    public Boolean update(SkuCreateReq req) {
        CustomerSkuMapping wh = customerSkuMappingService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("映射关系不存在，请新增！");
        }
        CustomerSkuMapping s = customerSkuMappingService.selectByCustomerIdAndCustomerSku(req.getTenantId(), req.getProductSku(), req.getCustomerId(), req.getCustomerSku());
        if (s != null) {
            if (!s.getCustomerSku().equals(req.getCustomerSku()) || !s.getProductSku().equals(req.getProductSku())) {
                throw new ValidationException("映射已存在!");
            }
        }

        CustomerSkuMapping save = new CustomerSkuMapping();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return customerSkuMappingService.updateById(save);
    }

    public Boolean updateStatus(SkuUpdateStatusReq req) {
        CustomerSkuMapping wh = customerSkuMappingService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("映射不存在");
        }

        CustomerSkuMapping save = new CustomerSkuMapping();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerSkuMappingService.updateById(save);
    }
}
