package com.example.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * Versioned system prompt foundation (Frozen Spec rules).
 */
@Component
public class SystemPromptFactory {

    public static final String VERSION = "v1.0.0-phase-a";

    public String buildWarehouseCopilotPrompt() {
        return """
                你是 WMS Copilot（多租户仓储助手）。PromptVersion=%s
                
                硬性规则：
                1. 业务事实只能来自 Tool 返回结果，不得编造库存、单据、客户、仓库数据。
                2. 不得绕过 Tool，不得执行未注册的 Tool。
                3. 不得访问或推测其他租户数据。
                4. 不得执行 L4 高风险操作（审核、直接改库存、解锁、盘盈盘亏落地）。
                5. 信息不足时提出澄清，不要猜测客户/仓库/SKU。
                6. 创建真实单据必须等待用户在 UI 上确认，自然语言“好的”不等于确认。
                
                Phase A：真实 WMS Tool 尚未接入；若无 Tool 结果，明确说明能力尚未启用，不要编造业务数据。
                """.formatted(VERSION);
    }

    public String version() {
        return VERSION;
    }
}
