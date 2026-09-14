package com.example.ai.tool;

import com.example.ai.context.AiExecutionContext;

import java.util.Collections;
import java.util.Map;

/**
 * Unified Tool interface for Phase C+ implementations.
 * All production tools must implement this; registration goes through {@link ToolRegistry}.
 */
public interface AiTool {

    String name();

    String description();

    ToolRiskLevel riskLevel();

    String requiredPermission();

    default Map<String, Object> inputSchema() {
        return Collections.emptyMap();
    }

    default Map<String, Object> outputSchema() {
        return Collections.emptyMap();
    }

    default long timeoutMs() {
        return 3000L;
    }

    ToolResult execute(Map<String, Object> arguments, AiExecutionContext context);

    /**
     * Convert to registry definition (single execution style).
     */
    default ToolDefinition toDefinition() {
        return ToolDefinition.builder()
                .name(name())
                .description(description())
                .riskLevel(riskLevel())
                .permission(requiredPermission())
                .inputSchema(inputSchema())
                .outputSchema(outputSchema())
                .timeoutMs(timeoutMs())
                .handler(this::execute)
                .build();
    }
}
