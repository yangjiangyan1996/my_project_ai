package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkAdjustOrderFacade;
import com.example.annotations.LogOperation;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.AdjustApproveOkReq;
import com.example.entity.cangku.req.AdjustListPageReq;
import com.example.entity.cangku.req.AdjustRequest;
import com.example.entity.cangku.req.AdjustSubmitApproveReq;
import com.example.entity.cangku.resp.AdjustBasicResp;
import com.example.entity.cangku.resp.AdjustDetailResp;
import com.example.entity.cangku.resp.AdjustOrderResp;
import com.example.entity.cangku.resp.AdjustExecuteResult;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/29 15:23
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/adjust")
public class CkAdjustOrderController {

    @Resource
    private CkAdjustOrderFacade ckAdjustOrderFacade;


    @PostMapping("/createManual")
    @LogOperation(module = "调整单管理", operation = "创建调整单",
            description = "创建新调整单")
    public RespBean<Boolean> createManual(@RequestBody AdjustRequest req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = ckAdjustOrderFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkAdjustOrderController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "调整单管理", operation = "审核",
            description = "提交审核调整单")
    @GetMapping("/submitApprove")
    public RespBean<Boolean> submitApprove(@RequestParam("id") Long id) {
        try {
            AdjustSubmitApproveReq req = new AdjustSubmitApproveReq();
            req.setId(id);
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());
            Boolean result = ckAdjustOrderFacade.submitApprove(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkAdjustOrderController#submitApprove,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#submitApprove,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "调整单管理", operation = "审核",
            description = "审核通过调整单")
    @PostMapping("/approveOk")
    public RespBean<Boolean> approveOk(@RequestBody AdjustApproveOkReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());
            Boolean result = ckAdjustOrderFacade.approveOk(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkAdjustOrderController#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "调整单管理", operation = "调整单执行",
            description = "调整单执行")
    @GetMapping("/execute")
    public RespBean<Boolean> execute(@RequestParam("id") Long id) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            Boolean result = ckAdjustOrderFacade.execute(id, userId, tenantId);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("CkAdjustOrderController#execute,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#execute,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 分页查询
     *
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/pageList")
    public RespBean<Page<AdjustOrderResp>> pageList(@RequestBody AdjustListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<AdjustOrderResp> result = ckAdjustOrderFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkAdjustOrderController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 获取调整单详情（包括明细）
     * GET /api/auth/adjust/detail?id={id}
     */
    @GetMapping("/detail")
    public RespBean<AdjustDetailResp> getAdjustOrderDetail(@RequestParam("id") Long id) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            AdjustDetailResp result = ckAdjustOrderFacade.getAdjustOrderDetail(id, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkAdjustOrderController#getAdjustOrderDetail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#getAdjustOrderDetail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取调整单基本信息
     * GET /api/auth/adjust/basic?id={id}
     */
    @GetMapping("/basic")
    public RespBean<AdjustBasicResp> getAdjustOrderBasic(@RequestParam Long id) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            AdjustBasicResp result = ckAdjustOrderFacade.getAdjustOrderBasic(id, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkAdjustOrderController#getAdjustOrderBasic,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#getAdjustOrderBasic,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取调整单执行结果
     * GET /api/auth/adjust/execute-result?id={id}
     */
    @GetMapping("/execute-result")
    public RespBean<AdjustExecuteResult> getExecuteResult(@RequestParam Long id) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            AdjustExecuteResult result = ckAdjustOrderFacade.queryExecuteResult(id, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkAdjustOrderController#getExecuteResult,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkAdjustOrderController#getExecuteResult,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
