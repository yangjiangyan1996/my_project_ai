package com.example.ai.tool;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Registered tool contract (Frozen Spec metadata).
 */
@Getter
@Builder
public class ToolDefinition {
    private final String name;
    private final String description;
    private final ToolRiskLevel riskLevel;
    private final String permission;
    @Builder.Default
    private final Map<String, Object> inputSchema = Collections.emptyMap();
    @Builder.Default
    private final Map<String, Object> outputSchema = Collections.emptyMap();
    @Builder.Default
    private final long timeoutMs = 3000L;
    @Builder.Default
    private final boolean audit = true;

    /**
     * Handler: (args, executionContext) -> ToolResult.
     * Production tools will call Facades; Phase A may leave null for metadata-only stubs.
     */
    private final BiFunction<Map<String, Object>, com.example.ai.context.AiExecutionContext, ToolResult> handler;
}
