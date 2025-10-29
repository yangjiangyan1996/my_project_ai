package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkWarehouseFacade;
import com.example.Facade.CommonFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.WareHouseResp;
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
 * @Date 2025/10/29 23:20
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/warehouse/")
public class WarehouseController {
    @Resource
    CommonFacade commonFacade;
    @Resource
    CkWarehouseFacade ckWarehouseFacade;

    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody WareHouseDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckWarehouseFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("WarehouseController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody WareHourseUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckWarehouseFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("WarehouseController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
    @PostMapping("/setDefault")
    public RespBean<Boolean> setDefault(@RequestBody WareHourseSetDefaultReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckWarehouseFacade.setDefault(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("WarehouseController#setDefault,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#setDefault,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody WareHourseCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckWarehouseFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("WarehouseController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody WareHourseCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckWarehouseFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("WarehouseController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/pageList")
    public RespBean<Page<WareHouseResp>> listOfWareHouse(@RequestBody WareHouseListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<WareHouseResp> result = ckWarehouseFacade.listOfWareHouse(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("WarehouseController#listOfWareHouse,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#listOfWareHouse,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/list")
    public RespBean<List<WareHouseResp>> list() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<WareHouseResp> result = ckWarehouseFacade.list(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("WarehouseController#list,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#list,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
