package com.example.ai.tool;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiToolException;
import com.example.ai.exception.AiValidationException;
import org.springframework.stereotype.Component;

/**
 * Risk gate for tool execution (Frozen Spec).
 */
@Component
public class ToolRiskPolicy {

    public void assertExecutable(ToolDefinition tool, AiExecutionContext ctx, String confirmToken) {
        if (tool == null || tool.getRiskLevel() == null) {
            throw new AiValidationException("工具风险等级未知");
        }
        ToolRiskLevel level = tool.getRiskLevel();
        switch (level) {
            case L0_READ, L1_ANALYSIS -> {
                // allowed after auth/permission
            }
            case L2_DRAFT -> {
                // Draft boundary: prepare_* only via registry; never auto L3 create.
            }
            case L3_CONFIRM_REQUIRED -> {
                if (confirmToken == null || confirmToken.isBlank()) {
                    throw new AiPermissionException("L3 工具需要 UI 确认令牌，禁止自然语言直接执行");
                }
                if (ctx.getConfirmToken() == null || !confirmToken.equals(ctx.getConfirmToken())) {
                    throw new AiPermissionException("确认令牌无效");
                }
            }
            case L4_HIGH_RISK -> throw new AiPermissionException("L4 高风险工具在 V1 中禁用");
            default -> throw new AiToolException("不支持的风险等级: " + level);
        }
    }
}
