package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.Tenant;
import com.example.mapper.CkTenantMapper;
import com.example.service.CkTenantService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:15
 */
@Service
public class CkTenantServiceImpl extends ServiceImpl<CkTenantMapper, Tenant> implements CkTenantService {
    @Override
    public Tenant selectById(Long tenantId) {
        return this.query()
                .eq("id", tenantId)
                .eq("is_deleted",0)
                .one();
    }
}
