package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.RecommendRule;
import com.example.entity.cangku.req.RecommendRuleListPageReq;
import com.example.mapper.CkRecommendRuleMapper;
import com.example.service.CkRecommendRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:39
 */
@Service
public class CkRecommendRuleServiceImpl extends ServiceImpl<CkRecommendRuleMapper, RecommendRule> implements CkRecommendRuleService {
    @Override
    public RecommendRule selectById(Long id, Long tenantId) {
        return this.query()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("is_deleted",0)
                .one();
    }

    @Override
    public Page<RecommendRule> getPage(Page<RecommendRule> page, RecommendRuleListPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<RecommendRule>()
                        .eq(req.getStatus()!= null ,"status", req.getStatus())
                        .eq(req.getApplyScene()!= null ,"apply_scene", req.getApplyScene())
                        .eq(req.getCustomerId()!= null ,"customer_id", req.getCustomerId())
                        .eq(req.getRuleType()!= null ,"rule_type", req.getRuleType())
                        .eq(req.getTriggerProductId()!= null ,"trigger_product_id", req.getTriggerProductId())
                        .like(StringUtils.isNotBlank(req.getRuleName() ), "rule_name", req.getRuleName())
                        .eq( "tenant_id", req.getTenantId())
                        .eq("is_deleted",0)
                        .orderByAsc("created_at")
        );
    }
}
