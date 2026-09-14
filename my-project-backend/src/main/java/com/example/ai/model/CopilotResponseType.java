package com.example.ai.model;

/**
 * Structured Copilot response types (Frozen Spec).
 */
public enum CopilotResponseType {
    TEXT,
    TOOL_RESULT,
    DRAFT,
    ERROR,
    PERMISSION_DENIED,
    NEED_CLARIFICATION
}
