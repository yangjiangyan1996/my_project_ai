package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CKProductFacade;
import com.example.Facade.CkInboundFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.InboundCreateReq;
import com.example.entity.cangku.req.InboundDeleteReq;
import com.example.entity.cangku.req.InboundListPageReq;
import com.example.entity.cangku.req.InboundUpdateStatusReq;
import com.example.entity.cangku.resp.InboundListPageResp;
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
 * @Date 2025/11/2 00:03
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/inbound/")
public class InboundController {
    @Resource
    CkInboundFacade ckInboundFacade;

    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody InboundCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = ckInboundFacade.create(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#create,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#create,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/pageList")
    public RespBean<Page<InboundListPageResp>> pageList(@RequestBody InboundListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<InboundListPageResp> result = ckInboundFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody InboundDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckInboundFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("InboundController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody InboundUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckInboundFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("InboundController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
