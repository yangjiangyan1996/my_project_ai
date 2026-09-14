package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallRecord {
        private String toolName;
        private String riskLevel;
        private boolean success;
        private String summary;
    }
}
