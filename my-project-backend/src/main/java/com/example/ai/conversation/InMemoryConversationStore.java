package com.example.ai.conversation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory conversation store for Phase A (no DB migration).
 * Replace with Redis in later phase per Frozen Spec.
 */
@Component
public class InMemoryConversationStore implements ConversationStore {

    private final Map<String, List<ConversationMessage>> store = new ConcurrentHashMap<>();

    private String key(String conversationId, Long tenantId) {
        return tenantId + ":" + conversationId;
    }

    @Override
    public void append(ConversationMessage message) {
        store.computeIfAbsent(key(message.getConversationId(), message.getTenantId()),
                k -> new ArrayList<>()).add(message);
    }

    @Override
    public List<ConversationMessage> list(String conversationId, Long tenantId) {
        List<ConversationMessage> list = store.get(key(conversationId, tenantId));
        if (list == null) {
            return List.of();
        }
        return List.copyOf(list);
    }

    @Override
    public void clear(String conversationId, Long tenantId) {
        store.remove(key(conversationId, tenantId));
    }

    /** Test helper. */
    public void clearAll() {
        store.clear();
    }

    public int size() {
        return store.values().stream().mapToInt(List::size).sum();
    }
}
