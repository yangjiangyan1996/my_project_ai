package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.example.Facade.CkSupplierFacade;
import com.example.entity.base.RespBean;
import com.example.entity.cangku.req.SupplierCreateReq;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/31 00:42
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/supplier/")
public class SupplierController {
    @Resource
    CkSupplierFacade supplierFacade;

    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody SupplierCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = supplierFacade.create(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("SupplierController#create,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#create,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}
