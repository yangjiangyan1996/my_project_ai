package com.example.Facade;

import com.example.entity.cangku.dto.Tenant;
import com.example.entity.cangku.req.TentantCreateReq;
import com.example.entity.cangku.resp.CkTenantResp;
import com.example.service.CkTenantService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
        CkTenantResp r = new CkTenantResp();
        r.setId(tenant.getId());
        r.setName(tenant.getName());
        r.setImage(tenant.getImage());
        r.setContactPerson(tenant.getContactPerson());
        r.setContactPhone(tenant.getContactPhone());
        r.setExpireAt(tenant.getExpireAt());
        r.setStatus(tenant.getStatus());
        r.setBossAuth(tenant.getContactId() != null && tenant.getContactId().equals(userId));
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TentantCreateReq req) {
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
}
