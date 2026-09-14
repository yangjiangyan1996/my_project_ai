package com.example.ai.tool;

/**
 * Standardized tool result / error codes for LLM-safe messaging.
 */
public enum ToolErrorCode {
    SUCCESS,
    BUSINESS_ERROR,
    VALIDATION_ERROR,
    PERMISSION_DENIED,
    NOT_FOUND,
    TIMEOUT,
    SYSTEM_ERROR
}
