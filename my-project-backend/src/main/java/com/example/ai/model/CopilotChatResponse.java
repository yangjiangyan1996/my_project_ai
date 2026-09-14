package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Structured Copilot API response (Frozen Spec).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotChatResponse {
    private CopilotResponseType type;
    private String message;
    private String conversationId;

    @Builder.Default
    private List<ToolCallRecord> toolCalls = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> cards = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> actions = new ArrayList<>();

    private Map<String, Object> draft;

    /** Aggregated usage for the request (tokens may be null). */
    private UsageSummary usage;

    /**
     * Dev-only tool chain trace. Null in non-dev profiles.
     */
    private List<String> debugToolTrace;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallRecord {
        private String toolCallId;
        private String toolName;
        private String riskLevel;
        private boolean success;
        private String summary;
        private String errorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageSummary {
        private Integer totalInputTokens;
        private Integer totalOutputTokens;
        private int totalLlmCalls;
        private int totalToolCalls;
        private long totalLatencyMs;
        private String model;
    }

    public static Map<String, Object> clarificationCard(String title, List<?> options) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("type", "clarification");
        card.put("title", title);
        card.put("options", options);
        return card;
    }
}
