package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.RecommendRule;
import com.example.entity.cangku.req.RecommendRuleListPageReq;
import com.example.entity.cangku.req.RecommendRuleQueryReq;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:38
 */
public interface CkRecommendRuleService extends IService<RecommendRule> {
    Page<RecommendRule> getPage(Page<RecommendRule> page, RecommendRuleListPageReq req);

    RecommendRule selectById(Long id, Long tenantId);

    List<RecommendRule> selectList(Long tenantId, RecommendRuleQueryReq req);

}
