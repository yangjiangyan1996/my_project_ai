package com.example.ai.tool;

import com.example.ai.audit.AiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiToolException;
import com.example.ai.permission.AiPermissionChecker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Executes registered tools only — never reflects arbitrary services.
 */
@Component
public class ToolExecutor {

    @Resource
    private ToolRegistry toolRegistry;
    @Resource
    private ToolRiskPolicy toolRiskPolicy;
    @Resource
    private AiPermissionChecker permissionChecker;
    @Resource
    private AiAuditRecorder auditRecorder;

    public ToolResult execute(ToolRequest request) {
        long start = System.currentTimeMillis();
        AiExecutionContext ctx = request.getExecutionContext();
        ToolDefinition tool = null;
        boolean success = false;
        String error = null;
        try {
            tool = toolRegistry.getRequired(request.getToolName());
            permissionChecker.checkToolPermission(ctx, tool);
            toolRiskPolicy.assertExecutable(tool, ctx, request.getConfirmToken());

            if (tool.getHandler() == null) {
                throw new AiToolException("工具尚未实现 handler: " + tool.getName());
            }
            Map<String, Object> args = request.getArguments() == null
                    ? Collections.emptyMap()
                    : request.getArguments();
            // Strip spoof tenant from args if present
            if (args.containsKey("tenantId")) {
                args = new java.util.HashMap<>(args);
                args.remove("tenantId");
            }
            ToolResult result = tool.getHandler().apply(args, ctx);
            success = result != null && result.isSuccess();
            return result;
        } catch (RuntimeException e) {
            error = e.getMessage();
            throw e;
        } finally {
            long latency = System.currentTimeMillis() - start;
            if (tool != null && tool.isAudit()) {
                auditRecorder.recordToolCall(
                        ctx,
                        tool.getName(),
                        tool.getRiskLevel().name(),
                        success,
                        latency,
                        error
                );
            }
        }
    }
}
