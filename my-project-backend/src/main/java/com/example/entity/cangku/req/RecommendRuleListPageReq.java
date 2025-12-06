package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/25 17:59
 */
@Data
public class RecommendRuleListPageReq extends PageReq {
    private String ruleName;
    private Long triggerProductId;
    private Long customerId;
    private Integer applyScene;
    private Integer ruleType;
    //private Integer isEnabled;
    private Integer status;
    Long userId;
    Long tenantId;
}
