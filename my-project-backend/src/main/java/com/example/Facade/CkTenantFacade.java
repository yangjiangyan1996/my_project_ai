package com.example.Facade;

import com.example.entity.cangku.dto.Tenant;
import com.example.entity.cangku.req.TenantCreateReq;
import com.example.entity.cangku.resp.CkTenantResp;
import com.example.entity.cangku.resp.CkTenantUnauthResp;
import com.example.service.CkTenantService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:14
 */
@Service
@Slf4j
public class CkTenantFacade {
    @Resource
    CkTenantService ckTenantService;

    public CkTenantResp getTenantInfo(Long tenantId, Long userId) {
        if (tenantId == null) {
            throw new RuntimeException("租户ID不能为空");
        }
        Tenant tenant = ckTenantService.selectById(tenantId);
        if (tenant == null) {
            throw new RuntimeException("租户不存在！");
        }
        CkTenantResp r = new CkTenantResp();
        r.setId(tenant.getId());
        r.setName(tenant.getName() == null ? "" : tenant.getName());
        r.setImage(tenant.getImage() == null ? "" : tenant.getImage());
        r.setContactPerson(tenant.getContactPerson() == null ? "" : tenant.getContactPerson());
        r.setContactPhone(tenant.getContactPhone() == null ? "" : tenant.getContactPhone());
        r.setExpireAt(tenant.getExpireAt() == null ? null : tenant.getExpireAt());
        r.setStatus(tenant.getStatus() == null ? 0 : tenant.getStatus());
        if (tenant.getContactId()!=null) {
            r.setBossAuth(tenant.getContactId() != null && tenant.getContactId().equals(userId));
        }
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TenantCreateReq req) {
        Tenant wh = ckTenantService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("机构不存在！");
        }
        wh.setName(req.getName());
        wh.setImage(req.getImage());
        wh.setStatus(req.getStatus());
        wh.setContactPerson(req.getContactPerson());
        wh.setContactPhone(req.getContactPhone());
        wh.setModifiedAt(new Date());
        wh.setModifiedBy(req.getUserId());

        return ckTenantService.updateById(wh);
    }

    public List<CkTenantUnauthResp> getTenantList() {
        List<Tenant> tenants = ckTenantService.selectAll();
        return tenants.stream().map(tenant -> {
            CkTenantUnauthResp r = new CkTenantUnauthResp();
            r.setId(tenant.getId());
            r.setName(tenant.getName());
            r.setImage(tenant.getImage());
            return r;
        }).toList();
    }
}
