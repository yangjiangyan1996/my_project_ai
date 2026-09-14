package com.example.ai.conversation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation store with bounded policy (Redis later).
 * Evicts by TTL / max sessions / max messages per conversation.
 */
@Component
public class InMemoryConversationStore implements ConversationStore {

    private final Map<String, SessionBucket> store = new ConcurrentHashMap<>();

    @Value("${ai.copilot.conversation.max-messages:40}")
    private int maxMessagesPerConversation;

    @Value("${ai.copilot.conversation.max-sessions:500}")
    private int maxSessions;

    @Value("${ai.copilot.conversation.ttl-hours:24}")
    private int ttlHours;

    private String key(String conversationId, Long tenantId) {
        return tenantId + ":" + conversationId;
    }

    @Override
    public void append(ConversationMessage message) {
        evictExpired();
        String k = key(message.getConversationId(), message.getTenantId());
        store.compute(k, (key, bucket) -> {
            if (bucket == null) {
                ensureSessionCapacity();
                bucket = new SessionBucket();
            }
            bucket.touch();
            bucket.messages.add(message);
            while (bucket.messages.size() > maxMessagesPerConversation) {
                bucket.messages.remove(0);
            }
            return bucket;
        });
    }

    @Override
    public List<ConversationMessage> list(String conversationId, Long tenantId) {
        SessionBucket bucket = store.get(key(conversationId, tenantId));
        if (bucket == null || bucket.isExpired(ttlHours)) {
            if (bucket != null) {
                store.remove(key(conversationId, tenantId));
            }
            return List.of();
        }
        bucket.touch();
        return List.copyOf(bucket.messages);
    }

    @Override
    public void clear(String conversationId, Long tenantId) {
        store.remove(key(conversationId, tenantId));
    }

    public void clearAll() {
        store.clear();
    }

    public int size() {
        return store.values().stream().mapToInt(b -> b.messages.size()).sum();
    }

    public int sessionCount() {
        return store.size();
    }

    private void ensureSessionCapacity() {
        if (store.size() < maxSessions) {
            return;
        }
        // Evict oldest touched session
        String oldestKey = null;
        Instant oldest = Instant.MAX;
        for (Map.Entry<String, SessionBucket> e : store.entrySet()) {
            if (e.getValue().lastTouched.isBefore(oldest)) {
                oldest = e.getValue().lastTouched;
                oldestKey = e.getKey();
            }
        }
        if (oldestKey != null) {
            store.remove(oldestKey);
        }
    }

    private void evictExpired() {
        Instant now = Instant.now();
        Duration ttl = Duration.ofHours(Math.max(1, ttlHours));
        Iterator<Map.Entry<String, SessionBucket>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SessionBucket> e = it.next();
            if (Duration.between(e.getValue().lastTouched, now).compareTo(ttl) > 0) {
                it.remove();
            }
        }
    }

    private static final class SessionBucket {
        private final List<ConversationMessage> messages = new ArrayList<>();
        private Instant lastTouched = Instant.now();

        void touch() {
            lastTouched = Instant.now();
        }

        boolean isExpired(int ttlHours) {
            return Duration.between(lastTouched, Instant.now()).toHours() >= Math.max(1, ttlHours);
        }
    }
}
