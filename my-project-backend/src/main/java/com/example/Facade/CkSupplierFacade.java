package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.Supplier;
import com.example.entity.cangku.req.SupplierCreateReq;
import com.example.entity.cangku.req.SupplierDeleteReq;
import com.example.entity.cangku.req.SupplierListPageReq;
import com.example.entity.cangku.req.SupplierUpdateStatusReq;
import com.example.entity.cangku.resp.SupplierPageListResp;
import com.example.service.CkSupplierService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/31 00:40
 */
@Service
public class CkSupplierFacade {

    @Resource
    private CkSupplierService supplierService;
    public Boolean create(SupplierCreateReq req) {
        if (req == null) {
            throw new ValidationException("参数错误");
        }

        Supplier s = supplierService.selectByTenantId(req.getTenantId(), req.getSupplierCode());
        if (s != null) {
            throw new ValidationException("供应商已存在");
        }
        s = new Supplier();
        s.setTenantId(req.getTenantId());
        s.setSupplierCode(req.getSupplierCode());
        s.setSupplierName(req.getSupplierName());
        s.setSupplierType(req.getSupplierType());
        s.setContactPerson(req.getContactPerson());
        s.setContactPhone(req.getContactPhone());
        s.setAddress(req.getAddress());
        s.setStatus(req.getStatus());
        s.setBankAccount(req.getBankAccount());
        s.setBankName(req.getBankName());
        s.setBusinessLicense(req.getBusinessLicense());
        s.setCooperationStatus(req.getCooperationStatus());
        s.setEmail(req.getEmail());
        s.setRemark(req.getRemark());
        s.setSupplierType(req.getSupplierType());
        s.setContactPerson(req.getContactPerson());

        s.setModifiedAt(new Date());
        s.setModifiedBy(req.getUserId());
        s.setCreatedAt(new Date());
        s.setCreatedBy(req.getUserId());
        return supplierService.save(s);
    }

    public Page<SupplierPageListResp> pageList(Page<Supplier> page, SupplierListPageReq req) {
        Page<Supplier> list = supplierService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
//        List<Long> whIds = list.getRecords().stream().map(v -> v.getWarehouseId()).collect(Collectors.toList());
//        List<Warehouse> byIds = wareHouseService.getByIds(whIds, req.getTenantId());
//        Map<Long, Warehouse> whMap = byIds.stream().collect(Collectors.toMap(Warehouse::getId, v -> v));

        List<SupplierPageListResp> collect = list.getRecords().stream().map(v -> {
            SupplierPageListResp p = new SupplierPageListResp();
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());

        Page<SupplierPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean update(SupplierCreateReq req) {
        Supplier s = supplierService.getById(req.getId());
        if (s == null) {
            throw new ValidationException("供应商不存在");
        }
        Supplier s2 = supplierService.selectByCodeOrName(req.getSupplierCode(),  req.getTenantId());
        if (s2 != null && !s2.getId().equals(s.getId())) {
            throw new ValidationException("供应商编码或名称已存在");
        }

        Supplier save = new Supplier();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());

        return supplierService.updateById(save);
    }

    public Boolean delete(SupplierDeleteReq req) {
        Supplier s = supplierService.getById(req.getId());
        if (s == null) {
            throw new ValidationException("供应商不存在");
        }

        Supplier save = new Supplier();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return supplierService.updateById(save);
    }

    public Boolean updateStatus(SupplierUpdateStatusReq req) {
        Supplier p = supplierService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("供应商不存在");
        }

        Supplier save = new Supplier();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return supplierService.updateById(save);
    }

    public List<SupplierPageListResp> listEnable(UserInfo user) {
        List<Supplier> list = supplierService.listWareHouseEnable(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(v -> {
            SupplierPageListResp p = new SupplierPageListResp();
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());
    }
}
