package com.example.ai.workspace;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.llm.LlmChatRequest;
import com.example.ai.llm.LlmChatResult;
import com.example.ai.llm.LlmClient;
import com.example.ai.llm.LlmMessage;
import com.example.ai.prompt.SystemPromptFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Daily Workspace API orchestration: WMS facts first, LLM summary best-effort.
 */
@Slf4j
@Service
public class DailyWorkspaceService {

    @Resource
    private DailyWorkspaceQueryService queryService;
    @Resource
    private LlmClient llmClient;
    @Resource
    private SystemPromptFactory systemPromptFactory;
    @Resource
    private ObjectMapper objectMapper;

    public AiDailyWorkspaceResponse load(AiExecutionContext ctx) {
        AiDailyWorkspaceData data = queryService.build(ctx);
        try {
            String factsJson = objectMapper.writeValueAsString(data);
            LlmChatResult result = llmClient.chat(LlmChatRequest.builder()
                    .systemPrompt(systemPromptFactory.buildDailyWorkspaceSummaryPrompt())
                    .messages(List.of(LlmMessage.builder()
                            .role("user")
                            .content("请根据以下 JSON 事实生成今日工作摘要（中文）。不得改写数字。\n" + factsJson)
                            .build()))
                    .tools(List.of())
                    .temperature(0.2)
                    .maxTokens(600)
                    .build());
            String summary = result.getContent() == null ? "" : result.getContent().trim();
            if (summary.isEmpty()) {
                return AiDailyWorkspaceResponse.builder()
                        .data(data)
                        .aiSummaryAvailable(false)
                        .aiSummaryError("EMPTY_SUMMARY")
                        .aiSummary("AI 摘要暂时不可用")
                        .build();
            }
            return AiDailyWorkspaceResponse.builder()
                    .data(data)
                    .aiSummary(summary)
                    .aiSummaryAvailable(true)
                    .build();
        } catch (Exception e) {
            log.warn("Daily workspace LLM summary failed: {}", e.getMessage());
            return AiDailyWorkspaceResponse.builder()
                    .data(data)
                    .aiSummaryAvailable(false)
                    .aiSummaryError("LLM_UNAVAILABLE")
                    .aiSummary("AI 摘要暂时不可用")
                    .build();
        }
    }
}
