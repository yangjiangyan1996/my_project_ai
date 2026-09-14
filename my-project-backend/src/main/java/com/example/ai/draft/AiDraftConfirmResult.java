package com.example.ai.draft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Confirm / cancel API response (safe fields only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDraftConfirmResult {
    private boolean success;
    private String errorCode;
    private String message;
    private String draftId;
    private DraftStatus draftStatus;
    private DraftType draftType;
    private Long businessOrderId;
    private String businessOrderNo;
    @Builder.Default
    private List<Map<String, Object>> actions = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> draft = new LinkedHashMap<>();

    public static AiDraftConfirmResult fail(String code, String message, AiDraft draft) {
        return AiDraftConfirmResult.builder()
                .success(false)
                .errorCode(code)
                .message(message)
                .draftId(draft == null ? null : draft.getDraftId())
                .draftStatus(draft == null ? null : draft.getStatus())
                .draftType(draft == null ? null : draft.getDraftType())
                .draft(draft == null ? Map.of() : AiDraftViews.toCard(draft))
                .build();
    }

    public static AiDraftConfirmResult ok(AiDraft draft, String message) {
        List<Map<String, Object>> actions = new ArrayList<>();
        String viewType = switch (draft.getDraftType()) {
            case INBOUND -> "VIEW_INBOUND";
            case OUTBOUND -> "VIEW_OUTBOUND";
            case STOCKTAKE -> "VIEW_STOCKTAKE";
        };
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("actionType", viewType);
        action.put("label", "查看单据");
        if (draft.getBusinessOrderId() != null) {
            action.put("entityId", draft.getBusinessOrderId());
        }
        actions.add(action);
        return AiDraftConfirmResult.builder()
                .success(true)
                .errorCode("SUCCESS")
                .message(message)
                .draftId(draft.getDraftId())
                .draftStatus(draft.getStatus())
                .draftType(draft.getDraftType())
                .businessOrderId(draft.getBusinessOrderId())
                .businessOrderNo(draft.getBusinessOrderNo())
                .actions(actions)
                .draft(AiDraftViews.toCard(draft))
                .build();
    }
}
