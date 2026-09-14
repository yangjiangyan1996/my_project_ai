package com.example.ai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Provider-agnostic tool call from an LLM turn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmToolCall {
    private String id;
    private String name;
    /** Parsed JSON object arguments (never raw trusted string alone). */
    @Builder.Default
    private Map<String, Object> arguments = Collections.emptyMap();
    /** Original arguments JSON string from provider (audit / debug). */
    private String argumentsJson;
}
