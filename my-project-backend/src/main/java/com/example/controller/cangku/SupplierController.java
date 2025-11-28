package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkSupplierFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.SupplierCreateReq;
import com.example.entity.cangku.req.SupplierDeleteReq;
import com.example.entity.cangku.req.SupplierListPageReq;
import com.example.entity.cangku.req.SupplierUpdateStatusReq;
import com.example.entity.cangku.resp.SupplierPageListResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody SupplierUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = supplierFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SupplierController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/pageList")
    public RespBean<Page<SupplierPageListResp>> pageList(@RequestBody SupplierListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<SupplierPageListResp> result = supplierFacade.pageList(Page.of(req.getPage() , req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("SupplierController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //获取可用供应商列表
    @GetMapping("/listEnable")
    public RespBean<List<SupplierPageListResp>> listEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<SupplierPageListResp> result = supplierFacade.listEnable(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("SupplierController#listEnable,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#listEnable,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

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

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody SupplierCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = supplierFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SupplierController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody SupplierDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = supplierFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SupplierController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SupplierController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}
