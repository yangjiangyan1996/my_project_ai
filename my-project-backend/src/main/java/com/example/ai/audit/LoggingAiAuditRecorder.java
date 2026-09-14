package com.example.ai.audit;

import com.example.ai.context.AiExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Phase A audit sink: structured logs without secrets / JWT / full payloads.
 */
@Slf4j
@Component
public class LoggingAiAuditRecorder implements AiAuditRecorder {

    @Override
    public void recordToolCall(AiExecutionContext ctx,
                               String toolName,
                               String riskLevel,
                               boolean success,
                               long latencyMs,
                               String errorSummary) {
        log.info("AI_AUDIT tool conversationId={} userId={} tenantId={} tool={} risk={} success={} latencyMs={} error={}",
                safe(ctx == null ? null : ctx.getConversationId()),
                ctx == null ? null : ctx.getUserId(),
                ctx == null ? null : ctx.getTenantId(),
                toolName,
                riskLevel,
                success,
                latencyMs,
                redact(errorSummary));
    }

    @Override
    public void recordChat(AiExecutionContext ctx,
                           String model,
                           long latencyMs,
                           boolean success,
                           String errorSummary) {
        log.info("AI_AUDIT chat conversationId={} userId={} tenantId={} model={} success={} latencyMs={} error={}",
                safe(ctx == null ? null : ctx.getConversationId()),
                ctx == null ? null : ctx.getUserId(),
                ctx == null ? null : ctx.getTenantId(),
                model,
                success,
                latencyMs,
                redact(errorSummary));
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }

    private String redact(String v) {
        if (v == null) {
            return null;
        }
        String lower = v.toLowerCase();
        if (lower.contains("bearer ") || lower.contains("password") || lower.contains("api key")
                || lower.contains("secret") || lower.contains("sk-")) {
            return "[REDACTED]";
        }
        return v.length() > 200 ? v.substring(0, 200) + "..." : v;
    }
}
