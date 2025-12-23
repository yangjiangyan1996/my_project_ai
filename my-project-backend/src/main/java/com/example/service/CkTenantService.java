package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.Tenant;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:15
 */
public interface CkTenantService extends IService<Tenant> {
    Tenant selectById(Long tenantId);
}
