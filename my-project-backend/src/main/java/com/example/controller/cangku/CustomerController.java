package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkCustomerFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.CustomerCreateReq;
import com.example.entity.cangku.req.CustomerDeleteReq;
import com.example.entity.cangku.req.CustomerListPageReq;
import com.example.entity.cangku.req.CustomerUpdateStatusReq;
import com.example.entity.cangku.resp.CustomerEnabledListResp;
import com.example.entity.cangku.resp.CustomerPageListResp;
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
 * @Date 2025/11/5 20:24
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/customer/")
public class CustomerController {

    @Resource
    CkCustomerFacade customerFacade;

    //获取可用商品列表
    @GetMapping("/listEnable")
    public RespBean<List<CustomerEnabledListResp>> listEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<CustomerEnabledListResp> result = customerFacade.listEnable(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CustomerController#listEnable", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#listEnable,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody CustomerCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = customerFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CustomerController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody CustomerDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = customerFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CustomerController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody CustomerUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = customerFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CustomerController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/pageList")
    public RespBean<Page<CustomerPageListResp>> pageList(@RequestBody CustomerListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<CustomerPageListResp> result = customerFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CustomerController#pageList,req:{}",JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#pageList,req:{}",JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody CustomerCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = customerFacade.create(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CustomerController#categoryCreate,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CustomerController#categoryCreate,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
