package com.example.ai.tool.query;

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

public abstract class AbstractWmsQueryTool implements AiTool {

    @Override
    public final ToolRiskLevel riskLevel() {
        return ToolRiskLevel.L0_READ;
    }

    @Override
    public final ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
        try {
            return doExecute(arguments == null ? Map.of() : arguments, context);
        } catch (AiValidationException e) {
            return ToolResult.fail(ToolErrorCode.VALIDATION_ERROR, e.getMessage());
        } catch (ValidationException e) {
            if (WmsQuerySupport.looksNotFound(e)) {
                return ToolResult.fail(ToolErrorCode.NOT_FOUND, e.getMessage());
            }
            return ToolResult.fail(ToolErrorCode.BUSINESS_ERROR, e.getMessage());
        } catch (NullPointerException e) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "记录不存在");
        }
    }

    protected abstract ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context);

    protected Map<String, Object> objectSchema(String... requiredAndProps) {
        // unused
        return Map.of();
    }

    protected static Map<String, Object> schema(List<String> required, Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", required);
        schema.put("properties", properties);
        return schema;
    }
}
