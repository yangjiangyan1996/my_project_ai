package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.RecommendRuleItem;
import com.example.mapper.CkRecommendRuleItemMapper;
import com.example.service.CkRecommendRuleItemService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/6 15:40
 */
@Service
public class CkRecommendRuleItemServiceImpl extends ServiceImpl<CkRecommendRuleItemMapper, RecommendRuleItem> implements CkRecommendRuleItemService {
    @Override
    public List<RecommendRuleItem> selectByRuleIds(Long tenantId, List<Long> ruleIds) {
        return this.baseMapper.selectList(new QueryWrapper<RecommendRuleItem>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0)
                .in("rule_id", ruleIds));
    }

    @Override
    public Boolean delectedByRuleId(Long ruleId, Long userId, Long tenantId) {
        RecommendRuleItem recommendRuleItem = new RecommendRuleItem();
        recommendRuleItem.setIsDeleted(1);
        recommendRuleItem.setModifiedAt(new Date());
        recommendRuleItem.setModifiedBy(userId);
        return this.baseMapper.update(recommendRuleItem, new QueryWrapper<RecommendRuleItem>()
                .eq("rule_id", ruleId)
                .eq("is_deleted", 0)
                .eq("tenant_id", tenantId)) > 0;
    }
}
