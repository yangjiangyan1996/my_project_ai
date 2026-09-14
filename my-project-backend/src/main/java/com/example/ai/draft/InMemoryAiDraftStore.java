package com.example.ai.draft;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for unit tests. Production uses {@link RedisAiDraftStore}.
 * If ever wired in prod without Redis → PRODUCTION_GAP.
 */
public class InMemoryAiDraftStore implements AiDraftStore {

    private final Map<String, AiDraft> store = new ConcurrentHashMap<>();

    @Override
    public void save(AiDraft draft) {
        store.put(key(draft.getTenantId(), draft.getUserId(), draft.getDraftId()), copy(draft));
    }

    @Override
    public Optional<AiDraft> find(Long tenantId, Long userId, String draftId) {
        AiDraft d = store.get(key(tenantId, userId, draftId));
        if (d == null) {
            return Optional.empty();
        }
        if (d.getExpireAt() != null && Instant.now().isAfter(d.getExpireAt())) {
            d.setStatus(DraftStatus.EXPIRED);
        }
        return Optional.of(copy(d));
    }

    @Override
    public void delete(Long tenantId, Long userId, String draftId) {
        store.remove(key(tenantId, userId, draftId));
    }

    @Override
    public String backendId() {
        return "INMEMORY";
    }

    public void clearAll() {
        store.clear();
    }

    private static String key(Long tenantId, Long userId, String draftId) {
        return tenantId + ":" + userId + ":" + draftId;
    }

    private static AiDraft copy(AiDraft d) {
        return AiDraft.builder()
                .draftId(d.getDraftId())
                .draftType(d.getDraftType())
                .status(d.getStatus())
                .conversationId(d.getConversationId())
                .userId(d.getUserId())
                .tenantId(d.getTenantId())
                .parsedInput(d.getParsedInput())
                .resolvedEntities(copyMap(d.getResolvedEntities()))
                .payload(copyMap(d.getPayload()))
                .display(copyMap(d.getDisplay()))
                .warnings(d.getWarnings() == null ? java.util.List.of() : new java.util.ArrayList<>(d.getWarnings()))
                .validationResult(d.getValidationResult())
                .confirmToken(d.getConfirmToken())
                .lastConfirmError(d.getLastConfirmError())
                .businessOrderId(d.getBusinessOrderId())
                .businessOrderNo(d.getBusinessOrderNo())
                .createdAt(d.getCreatedAt())
                .expireAt(d.getExpireAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private static Map<String, Object> copyMap(Map<String, Object> src) {
        if (src == null) {
            return new java.util.LinkedHashMap<>();
        }
        // LinkedHashMap allows null values (unlike Map.copyOf)
        return new java.util.LinkedHashMap<>(src);
    }
}
