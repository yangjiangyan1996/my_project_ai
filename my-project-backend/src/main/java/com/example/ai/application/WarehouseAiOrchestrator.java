package com.example.ai.application;

import com.example.ai.audit.AiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.conversation.ConversationMessage;
import com.example.ai.conversation.ConversationStore;
import com.example.ai.exception.AiException;
import com.example.ai.exception.AiModelException;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiValidationException;
import com.example.ai.llm.LlmChatRequest;
import com.example.ai.llm.LlmChatResult;
import com.example.ai.llm.LlmClient;
import com.example.ai.llm.LlmMessage;
import com.example.ai.llm.LlmToolCall;
import com.example.ai.model.CopilotChatRequest;
import com.example.ai.model.CopilotChatResponse;
import com.example.ai.model.CopilotResponseType;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiUserContext;
import com.example.ai.prompt.SystemPromptFactory;
import com.example.ai.tool.LlmToolSchemaAdapter;
import com.example.ai.tool.ToolCallGuard;
import com.example.ai.tool.ToolDefinition;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolExecutor;
import com.example.ai.tool.ToolRegistry;
import com.example.ai.tool.ToolRequest;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.ToolResultContextCompressor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-step tool-calling orchestrator:
 * User → LLM → (Tool → LLM)* → Final Answer
 * Explicit loop with max tool calls — no recursive chat().
 */
@Slf4j
@Service
public class WarehouseAiOrchestrator {

    private static final int MAX_HISTORY_MESSAGES = 20;

    @Resource
    private LlmClient llmClient;
    @Resource
    private AiUserContext aiUserContext;
    @Resource
    private AiPermissionChecker permissionChecker;
    @Resource
    private SystemPromptFactory systemPromptFactory;
    @Resource
    private ConversationStore conversationStore;
    @Resource
    private AiAuditRecorder auditRecorder;
    @Resource
    private ToolRegistry toolRegistry;
    @Resource
    private ToolExecutor toolExecutor;
    @Resource
    private ToolCallGuard toolCallGuard;
    @Resource
    private LlmToolSchemaAdapter toolSchemaAdapter;
    @Resource
    private ToolResultContextCompressor resultCompressor;
    @Resource
    private Environment environment;

    public CopilotChatResponse chat(CopilotChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new AiValidationException("消息不能为空");
        }

        AiExecutionContext ctx = aiUserContext.fromCurrentUser(
                request.getConversationId(),
                request.getContext());
        permissionChecker.requireAuthenticated(ctx);

        long start = System.currentTimeMillis();
        int llmCalls = 0;
        int toolCalls = 0;
        int inputTokens = 0;
        int outputTokens = 0;
        String model = llmClient.defaultModel();
        List<CopilotChatResponse.ToolCallRecord> records = new ArrayList<>();
        List<String> debugTrace = new ArrayList<>();

        try {
            aiUserContext.assertTenantNotSpoofed(request.getTenantId(), ctx.getTenantId());

            appendStore(ctx, "user", request.getMessage(), null, null);

            List<LlmMessage> messages = buildMessagesFromHistory(ctx);
            List<Map<String, Object>> tools = toolSchemaAdapter.toFunctionSchemas(toolRegistry.listAll());
            Map<String, AtomicInteger> fingerprints = toolCallGuard.newFingerprintTracker();

            // Max LLM rounds = maxToolCalls + 1 (final answer after tools)
            int maxRounds = toolCallGuard.getMaxToolCalls() + 1;
            for (int round = 0; round < maxRounds; round++) {
                LlmChatResult llmResult = llmClient.chat(LlmChatRequest.builder()
                        .systemPrompt(systemPromptFactory.buildWarehouseCopilotPrompt())
                        .messages(messages)
                        .tools(tools.isEmpty() ? List.of() : tools)
                        .temperature(0.2)
                        .build());
                llmCalls++;
                model = llmResult.getModel() != null ? llmResult.getModel() : model;
                if (llmResult.getInputTokens() != null) {
                    inputTokens += llmResult.getInputTokens();
                }
                if (llmResult.getOutputTokens() != null) {
                    outputTokens += llmResult.getOutputTokens();
                }

                if (!llmResult.hasToolCalls()) {
                    String content = llmResult.getContent() == null ? "" : llmResult.getContent();
                    appendStore(ctx, "assistant", content, null, null);
                    CopilotChatResponse resp = CopilotChatResponse.builder()
                            .type(CopilotResponseType.TEXT)
                            .message(content)
                            .conversationId(ctx.getConversationId())
                            .toolCalls(records)
                            .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                            .debugToolTrace(devTrace(debugTrace))
                            .build();
                    audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                            inputTokens, outputTokens, start, true, null);
                    return resp;
                }

                // Assistant turn with tool_calls
                List<LlmToolCall> calls = llmResult.safeToolCalls();
                messages.add(LlmMessage.builder()
                        .role("assistant")
                        .content(llmResult.getContent())
                        .toolCalls(calls)
                        .build());

                for (LlmToolCall call : calls) {
                    toolCallGuard.assertWithinLimit(toolCalls);
                    String fp = toolCallGuard.fingerprint(call.getName(), call.getArguments());
                    toolCallGuard.assertNotLooping(fingerprints, fp);

                    String toolCallId = StringUtils.hasText(call.getId())
                            ? call.getId()
                            : "call_" + UUID.randomUUID();
                    call.setId(toolCallId);

                    ToolResult result = executeTool(call, ctx);
                    toolCalls++;
                    debugTrace.add(call.getName());

                    ToolDefinition def = toolRegistry.get(call.getName());
                    String risk = def == null ? "UNKNOWN" : def.getRiskLevel().name();
                    records.add(CopilotChatResponse.ToolCallRecord.builder()
                            .toolCallId(toolCallId)
                            .toolName(call.getName())
                            .riskLevel(risk)
                            .success(result.isSuccess())
                            .summary(result.getMessage())
                            .errorCode(result.getErrorCode())
                            .build());

                    String compressed = resultCompressor.compressForLlm(result);
                    messages.add(LlmMessage.builder()
                            .role("tool")
                            .toolCallId(toolCallId)
                            .name(call.getName())
                            .content(compressed)
                            .build());
                    appendStore(ctx, "tool", compressed, toolCallId, call.getName());

                    if (isAmbiguous(result)) {
                        CopilotChatResponse resp = clarificationResponse(ctx, result, records, debugTrace,
                                model, inputTokens, outputTokens, llmCalls, toolCalls, start);
                        audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                                inputTokens, outputTokens, start, true, "AMBIGUOUS");
                        return resp;
                    }

                    if (isNeedClarification(result)) {
                        CopilotChatResponse resp = clarificationResponse(ctx, result, records, debugTrace,
                                model, inputTokens, outputTokens, llmCalls, toolCalls, start);
                        // prefer tool message when present
                        if (result.getMessage() != null && !result.getMessage().isBlank()) {
                            resp.setMessage(result.getMessage());
                        }
                        audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                                inputTokens, outputTokens, start, true, "NEED_CLARIFICATION");
                        return resp;
                    }

                    if (isDraftResult(result)) {
                        CopilotChatResponse resp = draftResponse(ctx, result, records, debugTrace,
                                model, inputTokens, outputTokens, llmCalls, toolCalls, start);
                        audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                                inputTokens, outputTokens, start, true, "DRAFT");
                        return resp;
                    }

                    if (ToolErrorCode.PERMISSION_DENIED.name().equals(result.getErrorCode())) {
                        CopilotChatResponse resp = CopilotChatResponse.builder()
                                .type(CopilotResponseType.PERMISSION_DENIED)
                                .message("你没有权限查看该数据。")
                                .conversationId(ctx.getConversationId())
                                .toolCalls(records)
                                .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                                .debugToolTrace(devTrace(debugTrace))
                                .build();
                        appendStore(ctx, "assistant", resp.getMessage(), null, null);
                        audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                                inputTokens, outputTokens, start, false, result.getErrorCode());
                        return resp;
                    }
                }
            }

            // Exhausted tool budget without final text
            String msg = "本轮工具调用次数已达上限，请缩小问题范围或稍后重试。";
            appendStore(ctx, "assistant", msg, null, null);
            CopilotChatResponse resp = CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message(msg)
                    .conversationId(ctx.getConversationId())
                    .toolCalls(records)
                    .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                    .debugToolTrace(devTrace(debugTrace))
                    .build();
            audit(ctx, model, resp.getType().name(), llmCalls, toolCalls,
                    inputTokens, outputTokens, start, false, "MAX_TOOL_CALLS");
            return resp;

        } catch (AiPermissionException e) {
            audit(ctx, model, CopilotResponseType.PERMISSION_DENIED.name(), llmCalls, toolCalls,
                    inputTokens, outputTokens, start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.PERMISSION_DENIED)
                    .message(e.getMessage())
                    .conversationId(ctx.getConversationId())
                    .toolCalls(records)
                    .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                    .debugToolTrace(devTrace(debugTrace))
                    .build();
        } catch (AiValidationException e) {
            String type = e.getMessage() != null && e.getMessage().contains("循环")
                    ? CopilotResponseType.NEED_CLARIFICATION.name()
                    : CopilotResponseType.ERROR.name();
            CopilotResponseType respType = type.equals(CopilotResponseType.NEED_CLARIFICATION.name())
                    ? CopilotResponseType.NEED_CLARIFICATION
                    : CopilotResponseType.ERROR;
            String userMsg = respType == CopilotResponseType.NEED_CLARIFICATION
                    ? "查询过程出现重复步骤，请补充更具体的商品/仓库信息后重试。"
                    : e.getMessage();
            audit(ctx, model, respType.name(), llmCalls, toolCalls,
                    inputTokens, outputTokens, start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(respType)
                    .message(userMsg)
                    .conversationId(ctx.getConversationId())
                    .toolCalls(records)
                    .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                    .debugToolTrace(devTrace(debugTrace))
                    .build();
        } catch (AiModelException e) {
            audit(ctx, model, CopilotResponseType.ERROR.name(), llmCalls, toolCalls,
                    inputTokens, outputTokens, start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message("模型调用失败，请稍后重试")
                    .conversationId(ctx.getConversationId())
                    .toolCalls(records)
                    .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                    .debugToolTrace(devTrace(debugTrace))
                    .build();
        } catch (AiException e) {
            audit(ctx, model, CopilotResponseType.ERROR.name(), llmCalls, toolCalls,
                    inputTokens, outputTokens, start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message(e.getMessage())
                    .conversationId(ctx.getConversationId())
                    .toolCalls(records)
                    .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                    .debugToolTrace(devTrace(debugTrace))
                    .build();
        }
    }

    private ToolResult executeTool(LlmToolCall call, AiExecutionContext ctx) {
        if (call.getName() == null || call.getName().isBlank()) {
            return ToolResult.fail(ToolErrorCode.VALIDATION_ERROR, "Tool 名称为空");
        }
        if (!toolRegistry.contains(call.getName())) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "未知工具: " + call.getName());
        }
        Map<String, Object> args = call.getArguments() == null
                ? Map.of()
                : new LinkedHashMap<>(call.getArguments());
        return toolExecutor.execute(ToolRequest.builder()
                .toolName(call.getName())
                .arguments(args)
                .executionContext(ctx)
                .build());
    }

    private boolean isAmbiguous(ToolResult result) {
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return false;
        }
        Object amb = result.getData().get("ambiguous");
        if (!Boolean.TRUE.equals(amb) && !"true".equalsIgnoreCase(String.valueOf(amb))) {
            return false;
        }
        Object items = result.getData().get("items");
        return items instanceof List<?> list && list.size() > 1;
    }

    private boolean isNeedClarification(ToolResult result) {
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return false;
        }
        Object flag = result.getData().get("needClarification");
        return Boolean.TRUE.equals(flag) || "true".equalsIgnoreCase(String.valueOf(flag));
    }

    private boolean isDraftResult(ToolResult result) {
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return false;
        }
        Object type = result.getData().get("responseType");
        return "DRAFT".equals(String.valueOf(type)) || result.getData().get("draft") != null;
    }

    @SuppressWarnings("unchecked")
    private CopilotChatResponse draftResponse(AiExecutionContext ctx,
                                              ToolResult result,
                                              List<CopilotChatResponse.ToolCallRecord> records,
                                              List<String> debugTrace,
                                              String model,
                                              int inputTokens,
                                              int outputTokens,
                                              int llmCalls,
                                              int toolCalls,
                                              long start) {
        Map<String, Object> draft = result.getData().get("draft") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        String message = result.getMessage() == null ? "已生成单据草稿，请确认后创建。" : result.getMessage();
        appendStore(ctx, "assistant", message, null, null);
        List<Map<String, Object>> actions = draft.get("actions") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        return CopilotChatResponse.builder()
                .type(CopilotResponseType.DRAFT)
                .message(message)
                .conversationId(ctx.getConversationId())
                .toolCalls(records)
                .draft(draft)
                .actions(actions)
                .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                .debugToolTrace(devTrace(debugTrace))
                .build();
    }

    @SuppressWarnings("unchecked")
    private CopilotChatResponse clarificationResponse(AiExecutionContext ctx,
                                                      ToolResult result,
                                                      List<CopilotChatResponse.ToolCallRecord> records,
                                                      List<String> debugTrace,
                                                      String model,
                                                      int inputTokens,
                                                      int outputTokens,
                                                      int llmCalls,
                                                      int toolCalls,
                                                      long start) {
        List<?> options = result.getData().get("items") instanceof List<?> list ? list : List.of();
        String message = "找到多个匹配结果，请选择其中一个后再继续。";
        appendStore(ctx, "assistant", message, null, null);
        return CopilotChatResponse.builder()
                .type(CopilotResponseType.NEED_CLARIFICATION)
                .message(message)
                .conversationId(ctx.getConversationId())
                .toolCalls(records)
                .cards(List.of(CopilotChatResponse.clarificationCard("请选择", options)))
                .usage(usage(model, inputTokens, outputTokens, llmCalls, toolCalls, start))
                .debugToolTrace(devTrace(debugTrace))
                .build();
    }

    private List<LlmMessage> buildMessagesFromHistory(AiExecutionContext ctx) {
        List<ConversationMessage> history = conversationStore.list(ctx.getConversationId(), ctx.getTenantId());
        List<ConversationMessage> window = history;
        if (history.size() > MAX_HISTORY_MESSAGES) {
            window = history.subList(history.size() - MAX_HISTORY_MESSAGES, history.size());
        }
        List<LlmMessage> messages = new ArrayList<>();
        for (ConversationMessage m : window) {
            if (m == null || m.getRole() == null) {
                continue;
            }
            // Prior-turn tool payloads are not replayed (would need assistant tool_calls pairing).
            // Multi-turn continuity comes from prior user/assistant text.
            if ("assistant".equalsIgnoreCase(m.getRole()) || "user".equalsIgnoreCase(m.getRole())) {
                messages.add(LlmMessage.builder()
                        .role(m.getRole().toLowerCase())
                        .content(m.getContent())
                        .build());
            }
        }
        return messages;
    }

    private void appendStore(AiExecutionContext ctx, String role, String content,
                             String toolCallId, String toolName) {
        conversationStore.append(ConversationMessage.builder()
                .conversationId(ctx.getConversationId())
                .messageId(UUID.randomUUID().toString())
                .role(role)
                .content(content)
                .toolCallId(toolCallId)
                .toolName(toolName)
                .createdAt(Instant.now())
                .userId(ctx.getUserId())
                .tenantId(ctx.getTenantId())
                .build());
    }

    private CopilotChatResponse.UsageSummary usage(String model, int inTok, int outTok,
                                                   int llmCalls, int toolCalls, long start) {
        return CopilotChatResponse.UsageSummary.builder()
                .model(model)
                .totalInputTokens(inTok == 0 ? null : inTok)
                .totalOutputTokens(outTok == 0 ? null : outTok)
                .totalLlmCalls(llmCalls)
                .totalToolCalls(toolCalls)
                .totalLatencyMs(System.currentTimeMillis() - start)
                .build();
    }

    private List<String> devTrace(List<String> trace) {
        if (environment != null && environment.matchesProfiles("dev")) {
            return List.copyOf(trace);
        }
        return null;
    }

    private void audit(AiExecutionContext ctx, String model, String type,
                       int llmCalls, int toolCalls, Integer inTok, Integer outTok,
                       long start, boolean success, String err) {
        auditRecorder.recordOrchestration(
                ctx, model, type, llmCalls, toolCalls, inTok, outTok,
                System.currentTimeMillis() - start, success, err);
    }
}
