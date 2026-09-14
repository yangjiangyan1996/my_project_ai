package com.example.ai.llm;

import com.example.ai.exception.AiModelException;
import com.example.ai.exception.AiValidationException;
import com.example.entity.deepseek.ChatRequest;
import com.example.entity.deepseek.DeepSeekResponse;
import com.example.utils.DeepSeekUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek adapter. Converts provider payloads ↔ internal {@link LlmChatResult}.
 * DeepSeek DTOs stay inside this class / DeepSeekUtils.
 */
@Component
public class DeepSeekLlmClient implements LlmClient {

    @Resource
    private DeepSeekUtils deepSeekUtils;
    @Resource
    private ObjectMapper objectMapper;

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    @Override
    public LlmChatResult chat(LlmChatRequest request) {
        if (request == null) {
            throw new AiModelException("LLM 请求不能为空");
        }
        long start = System.currentTimeMillis();
        try {
            ChatRequest providerReq = toProviderRequest(request);
            DeepSeekResponse response = deepSeekUtils.chatCompletions(providerReq);
            return toInternalResult(response, System.currentTimeMillis() - start);
        } catch (AiModelException e) {
            throw e;
        } catch (Exception e) {
            throw new AiModelException("LLM 调用失败: " + e.getMessage(), e);
        }
    }

    private ChatRequest toProviderRequest(LlmChatRequest request) {
        ChatRequest req = new ChatRequest();
        req.setModel(model);
        req.setTemperature(request.getTemperature() != null ? request.getTemperature() : 0.2);
        req.setMax_tokens(request.getMaxTokens() != null ? request.getMaxTokens() : 2000);

        List<ChatRequest.Message> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            messages.add(new ChatRequest.Message("system", request.getSystemPrompt()));
        }
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (LlmMessage m : request.getMessages()) {
                messages.add(toProviderMessage(m));
            }
        } else if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            messages.add(new ChatRequest.Message("user", request.getUserMessage()));
        }
        if (messages.isEmpty()) {
            throw new AiModelException("LLM messages 不能为空");
        }
        req.setMessages(messages);

        if (request.getTools() != null && !request.getTools().isEmpty()) {
            List<Map<String, Object>> tools = new ArrayList<>();
            for (Map<String, Object> t : request.getTools()) {
                tools.add(stripInternalMeta(t));
            }
            req.setTools(tools);
            req.setTool_choice("auto");
        }
        return req;
    }

    /**
     * Keep OpenAI-compatible function schema; drop internal risk/permission keys.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> stripInternalMeta(Map<String, Object> schema) {
        Map<String, Object> copy = new LinkedHashMap<>(schema);
        copy.remove("riskLevel");
        copy.remove("permission");
        Object fn = copy.get("function");
        if (fn instanceof Map<?, ?> fm) {
            Map<String, Object> function = new LinkedHashMap<>((Map<String, Object>) fm);
            function.remove("riskLevel");
            function.remove("permission");
            copy.put("function", function);
        }
        return copy;
    }

    private ChatRequest.Message toProviderMessage(LlmMessage m) {
        ChatRequest.Message out = new ChatRequest.Message();
        out.setRole(m.getRole());
        out.setContent(m.getContent());
        out.setName(m.getName());
        out.setTool_call_id(m.getToolCallId());
        if (m.getToolCalls() != null && !m.getToolCalls().isEmpty()) {
            List<ChatRequest.ToolCall> calls = new ArrayList<>();
            for (LlmToolCall tc : m.getToolCalls()) {
                ChatRequest.ToolCall p = new ChatRequest.ToolCall();
                p.setId(tc.getId());
                p.setType("function");
                ChatRequest.FunctionCall fn = new ChatRequest.FunctionCall();
                fn.setName(tc.getName());
                fn.setArguments(tc.getArgumentsJson() != null
                        ? tc.getArgumentsJson()
                        : writeJson(tc.getArguments()));
                p.setFunction(fn);
                calls.add(p);
            }
            out.setTool_calls(calls);
        }
        return out;
    }

    private LlmChatResult toInternalResult(DeepSeekResponse response, long latencyMs) {
        DeepSeekResponse.Choice choice = response.getChoices().get(0);
        DeepSeekResponse.Message msg = choice.getMessage();
        List<LlmToolCall> toolCalls = new ArrayList<>();
        if (msg != null && msg.getTool_calls() != null) {
            for (DeepSeekResponse.ToolCall tc : msg.getTool_calls()) {
                String argsJson = tc.getFunction() == null ? "{}" : tc.getFunction().getArguments();
                toolCalls.add(LlmToolCall.builder()
                        .id(tc.getId())
                        .name(tc.getFunction() == null ? null : tc.getFunction().getName())
                        .argumentsJson(argsJson)
                        .arguments(parseArgs(argsJson))
                        .build());
            }
        }
        Integer inTok = null;
        Integer outTok = null;
        if (response.getUsage() != null) {
            inTok = response.getUsage().getPrompt_tokens();
            outTok = response.getUsage().getCompletion_tokens();
        }
        String modelName = response.getModel() != null ? response.getModel() : model;
        return LlmChatResult.builder()
                .content(msg == null || msg.getContent() == null ? "" : msg.getContent())
                .model(modelName)
                .finishReason(choice.getFinish_reason())
                .toolCalls(toolCalls)
                .inputTokens(inTok)
                .outputTokens(outTok)
                .latencyMs(latencyMs)
                .build();
    }

    private Map<String, Object> parseArgs(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {
            });
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception e) {
            throw new AiValidationException("Tool 参数不是合法 JSON");
        }
    }

    private String writeJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map == null ? Map.of() : map);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String providerId() {
        return "deepseek";
    }

    @Override
    public String defaultModel() {
        return model;
    }
}
