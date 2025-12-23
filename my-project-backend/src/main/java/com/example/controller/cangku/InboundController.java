package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkCommentFacade;
import com.example.Facade.CkInboundFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.*;
import com.example.filter.UserUtil;
import com.example.annotations.LogOperation;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    CkCommentFacade commentFacade;
    @Resource
    CkInboundFacade ckInboundFacade;


    @LogOperation(module = "入库管理", operation = "审核入库",
            description = "审核通过入库单")
    @PostMapping("/approveOk")
    public RespBean<Boolean> approveOk(@RequestBody InboundApproveOkReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());
            Boolean result = ckInboundFacade.approveOk(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detail")
    public RespBean<InboundDetailResp> detail(@RequestParam("orderId") Long orderId) {
        try {
            InboundDetailResp result = ckInboundFacade.detail(orderId, UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取 生产入库单详情
     *
     * @param orderId
     * @return
     */
    @GetMapping("/detailOfProductionInbound")
    public RespBean<InboundProductInDetailResp> detailOfProductionInbound(@RequestParam("orderId") Long orderId) {
        try {
            InboundProductInDetailResp result = ckInboundFacade.detailOfProductionInbound(orderId, UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#detailOfProductionInbound,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#detailOfProductionInbound,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "入库管理", operation = "修改入库单",
            description = "修改入库单")
    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody InboundCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = ckInboundFacade.update(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "入库管理", operation = "创建入库单",
            description = "创建入库单")
    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody InboundCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = ckInboundFacade.create(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 入库管理页面展示数量
     *
     * @param req
     * @return
     */
    @PostMapping("/countsOfManagePage")
    public RespBean<InboundCountOfManagePageResp> list(@RequestBody InboundListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            InboundCountOfManagePageResp result = ckInboundFacade.countsOfManagePage(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#list", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#list", e);
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

            Page<InboundListPageResp> result = ckInboundFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "入库管理", operation = "删除入库单",
            description = "删除入库单")
    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody InboundDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckInboundFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "入库管理", operation = "修改入库单状态",
            description = "修改入库单状态")
    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody InboundUpdateStatusReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckInboundFacade.updateStatus(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InboundController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    //采购入库自动分配货架
    @PostMapping("/allocateIShelfnventoryQuantity")
    public RespBean<List<InboundProductUsedShelfResp>> allocateIShelfnventoryQuantity(@RequestBody InboundProductUsedShelfReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);

            List<InboundProductUsedShelfResp> result = commentFacade.allocateIShelfnventoryQuantity(req);
            return RespBean.success(result);
        }  catch (ValidationException e) {
            log.error("InboundController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
