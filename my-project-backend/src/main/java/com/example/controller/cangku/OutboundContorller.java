package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkOutboundFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.req.OutboundDeleteReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 01:03
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/outbound/")
public class OutboundContorller {

    @Resource
    CkOutboundFacade outboundFacade;

    @PostMapping("/pageList")
    public RespBean<Page<OutboundListPageResp>> pageList(@RequestBody OutboundListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<OutboundListPageResp> result = outboundFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detail")
    public RespBean<OutboundDetailResp> detail(@RequestParam("orderId") Long orderId) {
        try {
            OutboundDetailResp result = outboundFacade.detail(orderId, UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/approveOk")
    public RespBean<Boolean> approveOk(@RequestBody OutboundApproveOkReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());
            Boolean result = outboundFacade.approveOk(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody OutboundDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
