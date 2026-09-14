package com.example.ai.draft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Draft — not a WMS order. Bound to tenant + user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDraft {
    private String draftId;
    private DraftType draftType;
    private DraftStatus status;
    private String conversationId;
    private Long userId;
    private Long tenantId;
    private String parsedInput;
    @Builder.Default
    private Map<String, Object> resolvedEntities = new LinkedHashMap<>();
    /** Create-req shaped payload for Facade on confirm. */
    @Builder.Default
    private Map<String, Object> payload = new LinkedHashMap<>();
    /** Display fields for Draft Card (no secrets). */
    @Builder.Default
    private Map<String, Object> display = new LinkedHashMap<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    private DraftValidationResult validationResult;
    private String confirmToken;
    private String lastConfirmError;
    private Long businessOrderId;
    private String businessOrderNo;
    private Instant createdAt;
    private Instant expireAt;
    private Instant updatedAt;
}
