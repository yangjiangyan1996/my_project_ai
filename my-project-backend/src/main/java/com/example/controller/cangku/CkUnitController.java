package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkUnitFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.UnitCreateReq;
import com.example.entity.cangku.req.UnitDeleteReq;
import com.example.entity.cangku.req.UnitListPageReq;
import com.example.entity.cangku.req.UnitUpdateStatusReq;
import com.example.entity.cangku.resp.UnitPageListResp;
import com.example.filter.UserUtil;
import com.example.annotations.LogOperation;
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
 * @Date 2025/11/26 15:36
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/unit/")
public class CkUnitController {

    @Resource
    CkUnitFacade ckUnitFacade;

    @PostMapping("/pageList")
    public RespBean<Page<UnitPageListResp>> pageList(@RequestBody UnitListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<UnitPageListResp> result = ckUnitFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkUnitController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkUnitController#pageList,req:{}",JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/delete")
    @LogOperation(module = "单位管理", operation = "删除单位",
            description = "删除存在的单位")
    public RespBean<Boolean> delete(@RequestBody UnitDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckUnitFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkUnitController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkUnitController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/create")
    @LogOperation(module = "单位管理", operation = "创建单位",
            description = "创建新单位")
    public RespBean<Boolean> create(@RequestBody UnitCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckUnitFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkUnitController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkUnitController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/update")
    @LogOperation(module = "单位管理", operation = "更新单位",
            description = "更新存在的单位")
    public RespBean<Boolean> update(@RequestBody UnitCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckUnitFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkUnitController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkUnitController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    @LogOperation(module = "单位管理", operation = "更新单位状态",
            description = "更新存在的单位状态")
    public RespBean<Boolean> updateStatus(@RequestBody UnitUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckUnitFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkUnitController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkUnitController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}
