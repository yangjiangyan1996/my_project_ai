package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkAdjustOrderFacade;
import com.example.annotations.LogOperation;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.AdjustListPageReq;
import com.example.entity.cangku.req.AdjustRequest;
import com.example.entity.cangku.resp.AdjustOrderResp;
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
 * @Date 2025/12/29 15:23
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/adjust")
public class CkAdjustOrderController {

    @Resource
    private CkAdjustOrderFacade ckAdjustOrderFacade;


    @PostMapping("/create")
    @LogOperation(module = "单位管理", operation = "创建单位",
            description = "创建新单位")
    public RespBean<Boolean> create(@RequestBody AdjustRequest req) {
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
}
