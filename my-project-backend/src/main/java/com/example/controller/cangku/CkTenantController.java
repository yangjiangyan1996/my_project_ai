package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.example.Facade.CkTenantFacade;
import com.example.annotations.LogOperation;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.TenantCreateReq;
import com.example.entity.cangku.resp.CkTenantResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 15:11
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/tenant")
public class CkTenantController {
    @Resource
    private CkTenantFacade ckTenantFacade;

    @PostMapping("/update")
    @LogOperation(module = "机构管理", operation = "更新机构",
            description = "更新机构信息")
    public RespBean<Boolean> update(@RequestBody TenantCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckTenantFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkTenantController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkTenantController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/info")
    public RespBean<CkTenantResp> info() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            CkTenantResp r = ckTenantFacade.getTenantInfo(user.getTenantId(), user.getId());
            return RespBean.success(r);
        } catch (ValidationException e) {
            log.error("CkTenantController#info,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkTenantController#info,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
