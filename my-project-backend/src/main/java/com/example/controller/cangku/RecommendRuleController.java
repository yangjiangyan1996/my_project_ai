package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkRecommendRuleFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.ReCommendRuleDetailResp;
import com.example.entity.cangku.resp.ReCommendRulePageListResp;
import com.example.entity.cangku.resp.RecommendRuleItemDetailResp;
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
 * @Date 2025/11/3 22:49
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/recommend")
public class RecommendRuleController {
    @Resource
    private CkRecommendRuleFacade recommendRuleFacade;


    @PostMapping("/pageList")
    public RespBean<Page<ReCommendRulePageListResp>> pageList(@RequestBody RecommendRuleListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());
            Page<ReCommendRulePageListResp> result = recommendRuleFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("RecommendRuleController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detail")
    public RespBean<ReCommendRuleDetailResp> detail(@RequestParam("id") Long id) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            ReCommendRuleDetailResp result = recommendRuleFacade.detail(id, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("RecommendRuleController#detail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#detail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/items")
    public RespBean<List<RecommendRuleItemDetailResp>> items(@RequestParam("ruleId") Long ruleId) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<RecommendRuleItemDetailResp> result = recommendRuleFacade.items(ruleId, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("RecommendRuleController#items,req:{}", JSON.toJSONString(ruleId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#items,req:{}", JSON.toJSONString(ruleId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody CommendRuleDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = recommendRuleFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("RecommendRuleController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody RecommendRuleCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = recommendRuleFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("RecommendRuleController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody RecommendRuleCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = recommendRuleFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("RecommendRuleController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody RecommendRuleUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = recommendRuleFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("RecommendRuleController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("RecommendRuleController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
