package com.example.ai.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis draft store. Key: ai:wms:draft:{tenantId}:{userId}:{draftId}
 */
@Slf4j
@Component
public class RedisAiDraftStore implements AiDraftStore {

    public static final String KEY_PREFIX = "ai:wms:draft:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Value("${ai.copilot.draft.ttl-minutes:60}")
    private long ttlMinutes;

    @Override
    public void save(AiDraft draft) {
        String key = key(draft.getTenantId(), draft.getUserId(), draft.getDraftId());
        try {
            String json = objectMapper.writeValueAsString(draft);
            long ttl = Math.max(1, ttlMinutes);
            if (draft.getExpireAt() != null) {
                long seconds = Duration.between(Instant.now(), draft.getExpireAt()).getSeconds();
                if (seconds > 0) {
                    stringRedisTemplate.opsForValue().set(key, json, seconds, TimeUnit.SECONDS);
                    return;
                }
            }
            stringRedisTemplate.opsForValue().set(key, json, ttl, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new IllegalStateException("Draft Redis save failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AiDraft> find(Long tenantId, Long userId, String draftId) {
        String key = key(tenantId, userId, draftId);
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            AiDraft draft = objectMapper.readValue(json, AiDraft.class);
            if (draft.getExpireAt() != null && Instant.now().isAfter(draft.getExpireAt())) {
                draft.setStatus(DraftStatus.EXPIRED);
                return Optional.of(draft);
            }
            return Optional.of(draft);
        } catch (Exception e) {
            log.warn("Draft Redis read failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void delete(Long tenantId, Long userId, String draftId) {
        stringRedisTemplate.delete(key(tenantId, userId, draftId));
    }

    @Override
    public String backendId() {
        return "REDIS";
    }

    static String key(Long tenantId, Long userId, String draftId) {
        return KEY_PREFIX + tenantId + ":" + userId + ":" + draftId;
    }
}
