package com.example.ai.permission;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.ToolDefinition;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Frozen tasks.md B4 name. Delegates to {@link AiPermissionChecker}.
 */
@Component
public class AiToolPermissionGuard {

    @Resource
    private AiPermissionChecker permissionChecker;

    public void requireAuthenticated(AiExecutionContext ctx) {
        permissionChecker.requireAuthenticated(ctx);
    }

    public void check(AiExecutionContext ctx, ToolDefinition tool) {
        permissionChecker.checkToolPermission(ctx, tool);
    }
}
