package com.example.ai.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response: structured facts + optional LLM narrative (may be unavailable).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDailyWorkspaceResponse {
    private AiDailyWorkspaceData data;
    private String aiSummary;
    private boolean aiSummaryAvailable;
    private String aiSummaryError;
}
