package com.example.ai.llm;

import lombok.Builder;
import lombok.Data;

/**
 * Provider-agnostic LLM result.
 */
@Data
@Builder
public class LlmChatResult {
    private String content;
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private long latencyMs;
}
