package com.example.ai.draft;

import com.example.ai.audit.AiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiValidationException;
import com.example.ai.permission.AiPermissionChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draft lifecycle: create / cancel / confirm with tenant+user binding and CAS idempotency.
 * Confirm failure leaves draft OPEN (Phase G decision).
 */
@Slf4j
@Service
public class AiDraftService {

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Resource
    private AiDraftStore draftStore;
    @Resource
    private AiDraftOrderCreator orderCreator;
    @Resource
    private AiPermissionChecker permissionChecker;
    @Resource
    private AiAuditRecorder auditRecorder;
    @Resource
    private ObjectMapper objectMapper;

    @Value("${ai.copilot.draft.ttl-minutes:60}")
    private long ttlMinutes;

    public AiDraft create(AiExecutionContext ctx,
                          DraftType type,
                          String parsedInput,
                          Map<String, Object> resolvedEntities,
                          Map<String, Object> payload,
                          Map<String, Object> display,
                          java.util.List<String> warnings,
                          DraftValidationResult validation) {
        permissionChecker.requireAuthenticated(ctx);
        Instant now = Instant.now();
        AiDraft draft = AiDraft.builder()
                .draftId(UUID.randomUUID().toString())
                .draftType(type)
                .status(DraftStatus.OPEN)
                .conversationId(ctx.getConversationId())
                .userId(ctx.getUserId())
                .tenantId(ctx.getTenantId())
                .parsedInput(parsedInput)
                .resolvedEntities(resolvedEntities == null ? Map.of() : resolvedEntities)
                .payload(payload == null ? Map.of() : payload)
                .display(display == null ? Map.of() : display)
                .warnings(warnings == null ? java.util.List.of() : warnings)
                .validationResult(validation == null ? DraftValidationResult.OK : validation)
                .confirmToken(UUID.randomUUID().toString().replace("-", ""))
                .createdAt(now)
                .expireAt(now.plus(Math.max(1, ttlMinutes), ChronoUnit.MINUTES))
                .updatedAt(now)
                .build();
        draftStore.save(draft);
        auditRecorder.recordDraftEvent(ctx, "draft_created", draft.getDraftId(),
                type.name(), null, null, true, null);
        return draft;
    }

    public AiDraft requireOwned(AiExecutionContext ctx, String draftId) {
        permissionChecker.requireAuthenticated(ctx);
        if (draftId == null || draftId.isBlank()) {
            throw new AiValidationException("draftId 不能为空");
        }
        return draftStore.find(ctx.getTenantId(), ctx.getUserId(), draftId)
                .orElseThrow(() -> new AiValidationException("草稿不存在或无权访问"));
    }

    public AiDraftConfirmResult cancel(AiExecutionContext ctx, String draftId) {
        Object lock = locks.computeIfAbsent(lockKey(ctx, draftId), k -> new Object());
        synchronized (lock) {
            AiDraft draft = requireOwned(ctx, draftId);
            refreshExpired(draft);
            if (draft.getStatus() == DraftStatus.CANCELLED) {
                return AiDraftConfirmResult.fail("ALREADY_CANCELLED", "草稿已取消", draft);
            }
            if (draft.getStatus() == DraftStatus.CONFIRMED) {
                return AiDraftConfirmResult.fail("ALREADY_CONFIRMED", "草稿已确认创建，无法取消", draft);
            }
            if (draft.getStatus() == DraftStatus.EXPIRED) {
                draftStore.save(draft);
                return AiDraftConfirmResult.fail("DRAFT_EXPIRED", "草稿已过期", draft);
            }
            draft.setStatus(DraftStatus.CANCELLED);
            draft.setUpdatedAt(Instant.now());
            draftStore.save(draft);
            auditRecorder.recordDraftEvent(ctx, "draft_cancelled", draftId,
                    draft.getDraftType().name(), null, null, true, null);
            return AiDraftConfirmResult.builder()
                    .success(true)
                    .errorCode("SUCCESS")
                    .message("草稿已取消")
                    .draftId(draftId)
                    .draftStatus(DraftStatus.CANCELLED)
                    .draftType(draft.getDraftType())
                    .draft(AiDraftViews.toCard(draft))
                    .build();
        }
    }

    public AiDraftConfirmResult confirm(AiExecutionContext ctx, String draftId, String confirmToken) {
        Object lock = locks.computeIfAbsent(lockKey(ctx, draftId), k -> new Object());
        synchronized (lock) {
            AiDraft draft;
            try {
                draft = requireOwned(ctx, draftId);
            } catch (AiValidationException e) {
                auditRecorder.recordDraftEvent(ctx, "confirm_failed", draftId,
                        null, null, null, false, "NOT_FOUND");
                return AiDraftConfirmResult.fail("NOT_FOUND", e.getMessage(), null);
            }

            refreshExpired(draft);
            if (draft.getStatus() == DraftStatus.CONFIRMED) {
                auditRecorder.recordDraftEvent(ctx, "confirm_failed", draftId,
                        draft.getDraftType().name(), draft.getBusinessOrderId(),
                        draft.getBusinessOrderNo(), false, "ALREADY_CONFIRMED");
                return AiDraftConfirmResult.fail("ALREADY_CONFIRMED",
                        "该草稿已确认创建，不会重复创建单据", draft);
            }
            if (draft.getStatus() == DraftStatus.CANCELLED) {
                return AiDraftConfirmResult.fail("DRAFT_CANCELLED", "草稿已取消，无法确认", draft);
            }
            if (draft.getStatus() == DraftStatus.EXPIRED) {
                draftStore.save(draft);
                return AiDraftConfirmResult.fail("DRAFT_EXPIRED", "草稿已过期，请重新生成", draft);
            }
            if (draft.getValidationResult() == DraftValidationResult.BLOCK) {
                return AiDraftConfirmResult.fail("VALIDATION_BLOCK", "草稿校验未通过，无法创建", draft);
            }
            if (confirmToken == null || confirmToken.isBlank()
                    || draft.getConfirmToken() == null
                    || !draft.getConfirmToken().equals(confirmToken)) {
                auditRecorder.recordDraftEvent(ctx, "confirm_failed", draftId,
                        draft.getDraftType().name(), null, null, false, "INVALID_TOKEN");
                return AiDraftConfirmResult.fail("INVALID_CONFIRM_TOKEN", "确认令牌无效", draft);
            }

            String createPermission = createPermission(draft.getDraftType());
            try {
                assertCreatePermission(ctx, createPermission);
            } catch (AiPermissionException e) {
                auditRecorder.recordDraftEvent(ctx, "confirm_failed", draftId,
                        draft.getDraftType().name(), null, null, false, "PERMISSION_DENIED");
                return AiDraftConfirmResult.fail("PERMISSION_DENIED", e.getMessage(), draft);
            }

            // CAS: mark CONFIRMED before Facade call; on failure revert to OPEN
            draft.setStatus(DraftStatus.CONFIRMED);
            draft.setUpdatedAt(Instant.now());
            draft.setLastConfirmError(null);
            draftStore.save(draft);

            try {
                AiDraftOrderCreator.CreateOutcome outcome = orderCreator.create(ctx, draft);
                draft.setBusinessOrderId(outcome.orderId());
                draft.setBusinessOrderNo(outcome.orderNo());
                draft.setUpdatedAt(Instant.now());
                draftStore.save(draft);
                auditRecorder.recordDraftEvent(ctx, "draft_confirmed", draftId,
                        draft.getDraftType().name(), outcome.orderId(), outcome.orderNo(), true, null);
                String typeLabel = switch (draft.getDraftType()) {
                    case INBOUND -> "入库单";
                    case OUTBOUND -> "出库单";
                    case STOCKTAKE -> "盘点单";
                };
                return AiDraftConfirmResult.ok(draft,
                        typeLabel + "创建成功，单号：" + outcome.orderNo());
            } catch (Exception e) {
                String err = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                draft.setStatus(DraftStatus.OPEN);
                draft.setLastConfirmError(err);
                draft.setUpdatedAt(Instant.now());
                // rotate confirm token after failed attempt to reduce replay risk while allowing retry
                draft.setConfirmToken(UUID.randomUUID().toString().replace("-", ""));
                draftStore.save(draft);
                auditRecorder.recordDraftEvent(ctx, "confirm_failed", draftId,
                        draft.getDraftType().name(), null, null, false, truncate(err));
                log.warn("Draft confirm Facade failed draftId={} err={}", draftId, err);
                return AiDraftConfirmResult.fail("CREATE_FAILED",
                        "创建失败：" + err + "。草稿仍可重新确认。", draft);
            }
        }
    }

    public Map<String, Object> storeMeta() {
        return Map.of(
                "backend", draftStore.backendId(),
                "ttlMinutes", ttlMinutes
        );
    }

    private void refreshExpired(AiDraft draft) {
        if (draft.getStatus() == DraftStatus.OPEN
                && draft.getExpireAt() != null
                && Instant.now().isAfter(draft.getExpireAt())) {
            draft.setStatus(DraftStatus.EXPIRED);
            draft.setUpdatedAt(Instant.now());
        }
    }

    private void assertCreatePermission(AiExecutionContext ctx, String permission) {
        permissionChecker.requireAuthenticated(ctx);
        if (permission == null || permission.isBlank()) {
            return;
        }
        if (ctx.getPermissions() != null && !ctx.getPermissions().isEmpty()
                && !ctx.getPermissions().contains(permission)) {
            throw new AiPermissionException("无创建权限: " + permission);
        }
    }

    static String createPermission(DraftType type) {
        return switch (type) {
            case INBOUND -> "ck:inbound:create";
            case OUTBOUND -> "ck:outbound:create";
            case STOCKTAKE -> "ck:stocktake:create";
        };
    }

    private static String lockKey(AiExecutionContext ctx, String draftId) {
        return ctx.getTenantId() + ":" + ctx.getUserId() + ":" + draftId;
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 180 ? s.substring(0, 180) + "..." : s;
    }
}
