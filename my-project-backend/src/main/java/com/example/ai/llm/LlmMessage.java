package com.example.ai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {
    private String role;
    private String content;
    /** For role=tool — correlates to assistant tool_call id. */
    private String toolCallId;
    /** Optional tool name (some providers expect it on tool messages). */
    private String name;
    /** For role=assistant when requesting tools. */
    @Builder.Default
    private List<LlmToolCall> toolCalls = new ArrayList<>();
}
