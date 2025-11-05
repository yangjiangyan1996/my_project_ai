package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.req.CustomerCreateReq;
import com.example.entity.cangku.req.CustomerDeleteReq;
import com.example.entity.cangku.req.CustomerListPageReq;
import com.example.entity.cangku.req.CustomerUpdateStatusReq;
import com.example.entity.cangku.resp.CustomerEnabledListResp;
import com.example.entity.cangku.resp.CustomerPageListResp;
import com.example.enums.CkCustomerEnums;
import com.example.service.CkCustomerService;
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
 * @Date 2025/11/5 20:24
 */
@Service
public class CkCustomerFacade {

    @Resource
    private CkCustomerService customerService;
    public Boolean create(CustomerCreateReq req) {
        if (req == null || req.getTenantId() == null) {
            throw new ValidationException("参数错误");
        }

        Customer customer = customerService.selectByCode(req.getTenantId(), req.getCustomerCode());
        if (customer != null) {
            throw new ValidationException("客户code已存在");
        }

        customer = customerService.selectByName(req.getTenantId(), req.getCustomerName());
        if (customer != null) {
            throw new ValidationException("客户code已存在");
        }
        customer = new Customer();
        BeanUtils.copyProperties(req, customer);
        customer.setTenantId(req.getTenantId());
        customer.setModifiedAt(new Date());
        customer.setModifiedBy(req.getUserId());
        customer.setCreatedAt(new Date());
        customer.setCreatedBy(req.getUserId());
        return customerService.save(customer);
    }

    public Page<CustomerPageListResp> pageList(Page<Customer> page, CustomerListPageReq req) {
        Page<Customer> list = customerService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<CustomerPageListResp> collect = list.getRecords().stream().map(v -> {
            CustomerPageListResp p = new CustomerPageListResp();
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());

        Page<CustomerPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean updateStatus(CustomerUpdateStatusReq req) {
        Customer p = customerService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("客户不存在");
        }

        Customer save = new Customer();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerService.updateById(save);
    }

    public Boolean delete(CustomerDeleteReq req) {
        Customer p = customerService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("客户不存在");
        }

        Customer save = new Customer();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerService.updateById(save);
    }

    public Boolean update(CustomerCreateReq req) {
        Customer p = customerService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("客户不存在");
        }

        Customer save= new Customer();
        BeanUtils.copyProperties(req, save);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerService.updateById(save);
    }

    public List<CustomerEnabledListResp> listEnable(UserInfo user) {
        List<Customer> list = customerService.listEnable(user.getTenantId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(v -> {
            CustomerEnabledListResp p = new CustomerEnabledListResp();
            BeanUtils.copyProperties(v, p);

            p.setCustomerLevelName(CkCustomerEnums.CustomerLevel.getDescByCode(p.getCustomerLevel()));
            p.setCustomerTypeName(CkCustomerEnums.CustomerType.getDescByCode(p.getCustomerType()));
            return p;
        }).collect(Collectors.toList());
    }
}
