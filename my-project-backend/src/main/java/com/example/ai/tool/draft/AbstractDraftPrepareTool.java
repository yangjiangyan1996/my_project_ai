package com.example.ai.tool.draft;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.AiTool;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.ToolRiskLevel;
import jakarta.validation.ValidationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * L2 DRAFT tools — prepare_* only; never create real WMS orders.
 */
public abstract class AbstractDraftPrepareTool implements AiTool {

    @Override
    public final ToolRiskLevel riskLevel() {
        return ToolRiskLevel.L2_DRAFT;
    }

    @Override
    public final ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
        try {
            return doExecute(arguments == null ? Map.of() : arguments, context);
        } catch (AiValidationException e) {
            return ToolResult.fail(ToolErrorCode.VALIDATION_ERROR, e.getMessage());
        } catch (ValidationException e) {
            return ToolResult.fail(ToolErrorCode.BUSINESS_ERROR, e.getMessage());
        }
    }

    protected abstract ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context);

    protected static Map<String, Object> schema(List<String> required, Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", required);
        schema.put("properties", properties);
        return schema;
    }

    protected static ToolResult clarification(String message, List<?> options) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("needClarification", true);
        data.put("ambiguous", options != null && options.size() > 1);
        data.put("items", options == null ? List.of() : options);
        return ToolResult.ok(data, message);
    }
}
