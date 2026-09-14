package com.example.ai.audit;

import com.example.ai.context.AiExecutionContext;

/**
 * Audit contract for AI tool / chat events. Phase A: structured logging only.
 */
public interface AiAuditRecorder {

    void recordToolCall(AiExecutionContext ctx,
                        String toolName,
                        String riskLevel,
                        boolean success,
                        long latencyMs,
                        String errorSummary);

    void recordChat(AiExecutionContext ctx,
                    String model,
                    long latencyMs,
                    boolean success,
                    String errorSummary);
}
