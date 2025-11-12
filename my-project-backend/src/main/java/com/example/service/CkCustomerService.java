package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.req.CustomerListPageReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 20:25
 */
public interface CkCustomerService extends IService<Customer> {
    Customer selectByCode(Long tenantId, String customerCode);

    Customer selectByName(Long tenantId, String customerName);

    Page<Customer> getPage(Page<Customer> page, CustomerListPageReq req);

    List<Customer> listEnable(Long tenantId);

    List<Customer> selectByTenantIdAndCustomerIds(Long tenantId, List<Long> customerIds);

    Customer selectByTenantIdAndCustomerName(Long tenantId, String name);

}
