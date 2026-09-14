package com.example.ai.audit;

import com.example.ai.context.AiExecutionContext;

/**
 * Audit contract for AI tool / chat events.
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

    /**
     * End-of-request orchestration summary (Phase D).
     */
    default void recordOrchestration(AiExecutionContext ctx,
                                     String model,
                                     String responseType,
                                     int llmCalls,
                                     int toolCalls,
                                     Integer inputTokens,
                                     Integer outputTokens,
                                     long latencyMs,
                                     boolean success,
                                     String errorSummary) {
        recordChat(ctx, model, latencyMs, success,
                "type=" + responseType
                        + " llmCalls=" + llmCalls
                        + " toolCalls=" + toolCalls
                        + " inTok=" + inputTokens
                        + " outTok=" + outputTokens
                        + (errorSummary == null ? "" : " err=" + errorSummary));
    }

    /**
     * Draft lifecycle audit (Phase G). No full payload.
     */
    default void recordDraftEvent(AiExecutionContext ctx,
                                  String event,
                                  String draftId,
                                  String draftType,
                                  Long businessOrderId,
                                  String businessOrderNo,
                                  boolean success,
                                  String errorSummary) {
        recordChat(ctx, "draft", 0L, success,
                "event=" + event
                        + " draftId=" + draftId
                        + " draftType=" + draftType
                        + " orderId=" + businessOrderId
                        + " orderNo=" + businessOrderNo
                        + (errorSummary == null ? "" : " err=" + errorSummary));
    }
}
