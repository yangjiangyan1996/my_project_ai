package com.example.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Chat request from frontend. Tenant must NOT be trusted from this payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotChatRequest {
    private String conversationId;
    private String message;
    /** Optional UI context (page, orderId hint) — never authoritative tenant. */
    private Map<String, Object> context;
    /**
     * Ignored if present. Tenant comes from CurrentUser only.
     * Kept only so we can explicitly reject/overwrite attempts to spoof.
     */
    private Long tenantId;
}
