package com.example.ai.tool;

import com.example.ai.audit.AiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiBusinessException;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiToolException;
import com.example.ai.exception.AiValidationException;
import com.example.ai.permission.AiToolPermissionGuard;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Safe tool execution pipeline:
 * Registry → Permission → Risk → Input validation → Timeout → Handler → Audit
 * Never reflects arbitrary Spring beans.
 */
@Component
public class ToolExecutor {

    private final ExecutorService workerPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-tool-exec");
        t.setDaemon(true);
        return t;
    });

    @Resource
    private ToolRegistry toolRegistry;
    @Resource
    private ToolRiskPolicy toolRiskPolicy;
    @Resource
    private AiToolPermissionGuard permissionGuard;
    @Resource
    private ToolInputValidator inputValidator;
    @Resource
    private AiAuditRecorder auditRecorder;

    public ToolResult execute(ToolRequest request) {
        long start = System.currentTimeMillis();
        AiExecutionContext ctx = request.getExecutionContext();
        ToolDefinition tool = null;
        try {
            tool = toolRegistry.getRequired(request.getToolName());
            permissionGuard.check(ctx, tool);
            toolRiskPolicy.assertExecutable(tool, ctx, request.getConfirmToken());

            if (tool.getHandler() == null) {
                return finish(tool, ctx, start,
                        ToolResult.fail(ToolErrorCode.SYSTEM_ERROR, "工具尚未实现 handler: " + tool.getName()),
                        false, ToolErrorCode.SYSTEM_ERROR.name());
            }

            Map<String, Object> args = sanitizeArgs(request.getArguments());
            inputValidator.validate(tool.getInputSchema(), args);

            ToolResult result = invokeWithTimeout(tool, args, ctx);
            if (result == null) {
                return finish(tool, ctx, start,
                        ToolResult.fail(ToolErrorCode.SYSTEM_ERROR, "工具返回空结果"),
                        false, ToolErrorCode.SYSTEM_ERROR.name());
            }
            return finish(tool, ctx, start, result, result.isSuccess(),
                    result.isSuccess() ? null : result.getErrorCode());
        } catch (AiValidationException e) {
            return finish(tool, ctx, start,
                    ToolResult.fail(ToolErrorCode.VALIDATION_ERROR, e.getMessage()),
                    false, ToolErrorCode.VALIDATION_ERROR.name());
        } catch (AiPermissionException e) {
            return finish(tool, ctx, start,
                    ToolResult.fail(ToolErrorCode.PERMISSION_DENIED, e.getMessage()),
                    false, ToolErrorCode.PERMISSION_DENIED.name());
        } catch (AiBusinessException e) {
            return finish(tool, ctx, start,
                    ToolResult.fail(ToolErrorCode.BUSINESS_ERROR, e.getMessage()),
                    false, ToolErrorCode.BUSINESS_ERROR.name());
        } catch (AiToolException e) {
            ToolErrorCode code = (e.getMessage() != null && e.getMessage().startsWith("未知工具"))
                    ? ToolErrorCode.NOT_FOUND
                    : ToolErrorCode.SYSTEM_ERROR;
            return finish(tool, ctx, start,
                    ToolResult.fail(code, e.getMessage()),
                    false, code.name());
        } catch (RuntimeException e) {
            return finish(tool, ctx, start,
                    ToolResult.fail(ToolErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试"),
                    false, ToolErrorCode.SYSTEM_ERROR.name());
        }
    }

    private ToolResult invokeWithTimeout(ToolDefinition tool, Map<String, Object> args, AiExecutionContext ctx) {
        long timeout = tool.getTimeoutMs() > 0 ? tool.getTimeoutMs() : 3000L;
        Future<ToolResult> future = workerPool.submit(() -> tool.getHandler().apply(args, ctx));
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return ToolResult.fail(ToolErrorCode.TIMEOUT, "工具执行超时: " + tool.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ToolResult.fail(ToolErrorCode.SYSTEM_ERROR, "工具执行被中断");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof AiValidationException ve) {
                throw ve;
            }
            if (cause instanceof AiPermissionException pe) {
                throw pe;
            }
            if (cause instanceof AiBusinessException be) {
                throw be;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new AiToolException("工具执行失败: " + cause.getMessage(), cause);
        }
    }

    private Map<String, Object> sanitizeArgs(Map<String, Object> arguments) {
        Map<String, Object> args = arguments == null ? new HashMap<>() : new HashMap<>(arguments);
        args.remove("tenantId");
        args.remove("companyId");
        args.remove("userId");
        args.remove("permissions");
        return Collections.unmodifiableMap(args);
    }

    private ToolResult finish(ToolDefinition tool,
                              AiExecutionContext ctx,
                              long start,
                              ToolResult result,
                              boolean success,
                              String errorType) {
        long latency = System.currentTimeMillis() - start;
        if (tool != null && tool.isAudit()) {
            auditRecorder.recordToolCall(
                    ctx,
                    tool.getName(),
                    tool.getRiskLevel().name(),
                    success,
                    latency,
                    errorType
            );
        }
        return result;
    }

    @PreDestroy
    public void shutdown() {
        workerPool.shutdownNow();
    }
}
