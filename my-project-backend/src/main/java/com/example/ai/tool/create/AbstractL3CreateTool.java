package com.example.ai.tool.create;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.draft.AiDraftService;
import com.example.ai.draft.DraftType;
import com.example.ai.tool.AiTool;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.ToolRiskLevel;
import com.example.ai.tool.query.WmsQuerySupport;
import jakarta.annotation.Resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * L3 create tools — NOT registered into LLM ToolRegistry.
 * Only invocable from Draft confirm flow with matching confirmToken.
 */
public abstract class AbstractL3CreateTool implements AiTool {

    @Resource
    protected AiDraftService draftService;

    @Override
    public final ToolRiskLevel riskLevel() {
        return ToolRiskLevel.L3_CONFIRM_REQUIRED;
    }

    @Override
    public final ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
        if (context.getConfirmToken() == null || context.getConfirmToken().isBlank()) {
            return ToolResult.fail(ToolErrorCode.PERMISSION_DENIED, "L3 需要 UI 确认令牌");
        }
        String draftId = WmsQuerySupport.str(arguments, "draftId");
        if (draftId == null) {
            draftId = context.getConfirmDraftId();
        }
        if (draftId == null) {
            return ToolResult.fail(ToolErrorCode.VALIDATION_ERROR, "缺少 draftId");
        }
        var result = draftService.confirm(context, draftId, context.getConfirmToken());
        if (!result.isSuccess()) {
            return ToolResult.fail(result.getErrorCode(), result.getMessage());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("businessOrderId", result.getBusinessOrderId());
        data.put("businessOrderNo", result.getBusinessOrderNo());
        data.put("draftId", draftId);
        return ToolResult.ok(data, result.getMessage());
    }

    protected abstract DraftType expectedType();

    protected static Map<String, Object> schema(List<String> required, Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", required);
        schema.put("properties", properties);
        return schema;
    }
}
