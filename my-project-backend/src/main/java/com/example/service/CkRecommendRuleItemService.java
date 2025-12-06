package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.RecommendRuleItem;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:38
 */
public interface CkRecommendRuleItemService extends IService<RecommendRuleItem> {
    Boolean delectedByRuleId(Long ruleId, Long userId, Long tenantId);

    List<RecommendRuleItem> selectByRuleIds(Long tenantId, List<Long> ruleIds);
}
