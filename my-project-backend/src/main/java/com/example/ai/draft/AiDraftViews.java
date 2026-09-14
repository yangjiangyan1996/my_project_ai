package com.example.ai.draft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Safe Draft Card payload for Copilot DRAFT responses / FE.
 */
public final class AiDraftViews {

    private AiDraftViews() {
    }

    public static Map<String, Object> toCard(AiDraft draft) {
        Map<String, Object> card = new LinkedHashMap<>();
        if (draft == null) {
            return card;
        }
        card.put("draftId", draft.getDraftId());
        card.put("draftType", draft.getDraftType() == null ? null : draft.getDraftType().name());
        card.put("status", draft.getStatus() == null ? null : draft.getStatus().name());
        card.put("confirmToken", draft.getConfirmToken());
        card.put("validation", draft.getValidationResult() == null ? null : draft.getValidationResult().name());
        card.put("warnings", draft.getWarnings() == null ? List.of() : List.copyOf(draft.getWarnings()));
        card.put("fields", draft.getDisplay() == null ? Map.of() : new LinkedHashMap<>(draft.getDisplay()));
        Object items = draft.getDisplay() == null ? null : draft.getDisplay().get("items");
        card.put("items", items instanceof List<?> list ? list : List.of());
        card.put("expireAt", draft.getExpireAt() == null ? null : draft.getExpireAt().toString());
        card.put("lastConfirmError", draft.getLastConfirmError());
        card.put("businessOrderId", draft.getBusinessOrderId());
        card.put("businessOrderNo", draft.getBusinessOrderNo());
        List<Map<String, Object>> actions = new ArrayList<>();
        if (draft.getStatus() == DraftStatus.OPEN
                && draft.getValidationResult() != DraftValidationResult.BLOCK) {
            actions.add(action("EDIT_DRAFT", "修改"));
            actions.add(action("CANCEL_DRAFT", "取消"));
            actions.add(action("CONFIRM_DRAFT", "确认创建"));
        } else if (draft.getStatus() == DraftStatus.OPEN) {
            actions.add(action("EDIT_DRAFT", "修改"));
            actions.add(action("CANCEL_DRAFT", "取消"));
        }
        card.put("actions", actions);
        return card;
    }

    private static Map<String, Object> action(String type, String label) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("actionType", type);
        a.put("label", label);
        return a;
    }
}
