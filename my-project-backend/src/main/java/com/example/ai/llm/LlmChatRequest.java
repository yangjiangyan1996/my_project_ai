package com.example.ai.llm;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provider-agnostic LLM chat request (no DeepSeek types).
 */
@Data
@Builder
public class LlmChatRequest {
    private String systemPrompt;
    private String userMessage;

    @Builder.Default
    private List<LlmMessage> messages = new ArrayList<>();

    /** Optional tool metadata for future tool-calling models. */
    @Builder.Default
    private List<Map<String, Object>> tools = new ArrayList<>();

    private Double temperature;
    private Integer maxTokens;
}
