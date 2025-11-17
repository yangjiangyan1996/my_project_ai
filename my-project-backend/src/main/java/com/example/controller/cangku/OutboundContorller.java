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



    /**
     * 分页查询出库列表
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/pageList") // POST映射到/pageList路径
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



    /**
     * 获取出库详情
     * @param orderId 出库订单ID
     * @return 返回出库详情
     */
    @GetMapping("/detail") // GET映射到/detail路径
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



    /**
     * 创建出库单
     * @param req 创建出库单请求参数
     * @return 返回是否创建成功
     */
    @PostMapping("/create") // POST映射到/create路径
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





    /**
     * 更新出库单
     * @param req 更新出库单请求参数
     * @return 返回是否更新成功
     */
    @PostMapping("/update") // POST映射到/update路径
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




    /**
     * 审核通过出库单
     * @param req 审核通过请求参数
     * @return 返回是否审核成功
     */
    @PostMapping("/approveOk") // POST映射到/approveOk路径
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



    /**
     * 删除出库单
     * @param req 删除出库单请求参数
     * @return 返回是否删除成功
     */
    @PostMapping("/delete") // POST映射到/delete路径
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
