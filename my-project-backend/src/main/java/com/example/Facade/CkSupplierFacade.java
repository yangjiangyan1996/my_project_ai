package com.example.Facade;

import com.example.entity.cangku.dto.Supplier;
import com.example.entity.cangku.req.SupplierCreateReq;
import com.example.service.CkSupplierService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Date;

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
}
