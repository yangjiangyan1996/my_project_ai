package com.example.ai.draft;

import java.util.Optional;

/**
 * Draft persistence. Key must encode tenant + user + draftId.
 */
public interface AiDraftStore {

    void save(AiDraft draft);

    Optional<AiDraft> find(Long tenantId, Long userId, String draftId);

    void delete(Long tenantId, Long userId, String draftId);

    String backendId();
}
