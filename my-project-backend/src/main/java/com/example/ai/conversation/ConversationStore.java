package com.example.ai.conversation;

import java.util.List;

/**
 * Conversation persistence abstraction. No MySQL schema in Phase A.
 */
public interface ConversationStore {

    void append(ConversationMessage message);

    List<ConversationMessage> list(String conversationId, Long tenantId);

    void clear(String conversationId, Long tenantId);
}
