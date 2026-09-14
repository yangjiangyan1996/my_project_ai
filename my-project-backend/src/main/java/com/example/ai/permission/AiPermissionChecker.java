package com.example.ai.permission;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.tool.ToolDefinition;
import org.springframework.stereotype.Component;

/**
 * Interim permission boundary (Frozen Spec).
 * Phase A: authenticated + tenant required.
 * Phase H: enforce declared tool permission codes.
 */
@Component
public class AiPermissionChecker {

    public void requireAuthenticated(AiExecutionContext ctx) {
        if (ctx == null || ctx.getUserId() == null || ctx.getTenantId() == null) {
            throw new AiPermissionException("未登录或缺少租户上下文");
        }
    }

    /**
     * Check tool permission. Interim: allow if authenticated (REST parity).
     * When permissions set is non-empty, require declared permission.
     */
    public void checkToolPermission(AiExecutionContext ctx, ToolDefinition tool) {
        requireAuthenticated(ctx);
        if (tool == null) {
            throw new AiPermissionException("未知工具");
        }
        String required = tool.getPermission();
        if (required == null || required.isBlank()) {
            return;
        }
        // Phase H gap: when permission map populated, deny if missing.
        if (ctx.getPermissions() != null && !ctx.getPermissions().isEmpty()
                && !ctx.getPermissions().contains(required)) {
            throw new AiPermissionException("无权限执行工具: " + tool.getName() + " (" + required + ")");
        }
        // TODO Phase H: load ck_role/menu or Account.role → permission map
    }
}
