package com.example.ai.tool;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;

import java.util.Map;

/**
 * Test-only echo tool. Must not be registered in production catalog.
 */
public final class TestEchoToolFactory {

    private TestEchoToolFactory() {
    }

    public static ToolDefinition createEchoL0() {
        return ToolDefinition.builder()
                .name("test_echo")
                .description("test echo")
                .riskLevel(ToolRiskLevel.L0_READ)
                .permission("ck:ai:test")
                .timeoutMs(1000L)
                .handler((args, ctx) -> ToolResult.ok(
                        Map.of("echo", args.getOrDefault("text", ""),
                                "tenantId", ctx.getTenantId()),
                        "ok"))
                .build();
    }

    public static ToolDefinition createL3Confirm() {
        return ToolDefinition.builder()
                .name("test_confirm_action")
                .description("test L3")
                .riskLevel(ToolRiskLevel.L3_CONFIRM_REQUIRED)
                .permission("ck:ai:test")
                .handler((args, ctx) -> ToolResult.ok(Map.of("done", true), "confirmed"))
                .build();
    }

    public static ToolDefinition createL4Illegal() {
        return ToolDefinition.builder()
                .name("test_mutate_stock")
                .description("should not register")
                .riskLevel(ToolRiskLevel.L4_HIGH_RISK)
                .permission("ck:ai:test")
                .handler((args, ctx) -> ToolResult.ok(Map.of(), "no"))
                .build();
    }
}
