package com.example.ai.llm;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provider-agnostic LLM result (supports tool calling).
 */
@Data
@Builder
public class LlmChatResult {
    private String content;
    private String model;
    /** stop | tool_calls | length | content_filter | null */
    private String finishReason;
    private Integer inputTokens;
    private Integer outputTokens;
    private long latencyMs;

    @Builder.Default
    private List<LlmToolCall> toolCalls = new ArrayList<>();

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public List<LlmToolCall> safeToolCalls() {
        return toolCalls == null ? Collections.emptyList() : toolCalls;
    }
}
