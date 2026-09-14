package com.example.ai.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * AI execution context. Tenant/user always from auth — never from LLM args.
 */
@Getter
@Builder
public class AiExecutionContext {
    private final Long userId;
    private final Long tenantId;
    private final String username;
    private final String role;
    private final String conversationId;
    private final String requestId;

    @Builder.Default
    private final Set<String> permissions = Collections.emptySet();

    @Builder.Default
    private final Map<String, Object> requestMetadata = Collections.emptyMap();

    /** One-time confirm token for L3 tools (Phase G); optional in Phase A. */
    private final String confirmToken;
    private final String confirmDraftId;
}
