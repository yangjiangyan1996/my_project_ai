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
import com.example.ai.model.CopilotChatRequest;
import com.example.ai.model.CopilotChatResponse;
import com.example.ai.model.CopilotResponseType;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiUserContext;
import com.example.ai.prompt.SystemPromptFactory;
import com.example.ai.tool.ToolRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrator only — no Mapper / inventory / Facade calls.
 */
@Slf4j
@Service
public class WarehouseAiOrchestrator {

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

    public CopilotChatResponse chat(CopilotChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new AiValidationException("消息不能为空");
        }

        AiExecutionContext ctx = aiUserContext.fromCurrentUser(
                request.getConversationId(),
                request.getContext());
        permissionChecker.requireAuthenticated(ctx);

        long start = System.currentTimeMillis();
        try {
            aiUserContext.assertTenantNotSpoofed(request.getTenantId(), ctx.getTenantId());

            conversationStore.append(ConversationMessage.builder()
                    .conversationId(ctx.getConversationId())
                    .messageId(UUID.randomUUID().toString())
                    .role("user")
                    .content(request.getMessage())
                    .createdAt(Instant.now())
                    .userId(ctx.getUserId())
                    .tenantId(ctx.getTenantId())
                    .build());

            LlmChatResult llmResult = llmClient.chat(LlmChatRequest.builder()
                    .systemPrompt(systemPromptFactory.buildWarehouseCopilotPrompt())
                    .userMessage(request.getMessage())
                    .tools(toolRegistry.toLlmToolMetadata())
                    .build());

            String content = llmResult.getContent() == null ? "" : llmResult.getContent();
            conversationStore.append(ConversationMessage.builder()
                    .conversationId(ctx.getConversationId())
                    .messageId(UUID.randomUUID().toString())
                    .role("assistant")
                    .content(content)
                    .createdAt(Instant.now())
                    .userId(ctx.getUserId())
                    .tenantId(ctx.getTenantId())
                    .build());

            auditRecorder.recordChat(ctx, llmResult.getModel(), System.currentTimeMillis() - start, true, null);

            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.TEXT)
                    .message(content)
                    .conversationId(ctx.getConversationId())
                    .build();
        } catch (AiPermissionException e) {
            auditRecorder.recordChat(ctx, llmClient.defaultModel(), System.currentTimeMillis() - start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.PERMISSION_DENIED)
                    .message(e.getMessage())
                    .conversationId(ctx.getConversationId())
                    .build();
        } catch (AiValidationException e) {
            auditRecorder.recordChat(ctx, llmClient.defaultModel(), System.currentTimeMillis() - start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message(e.getMessage())
                    .conversationId(ctx.getConversationId())
                    .build();
        } catch (AiModelException e) {
            auditRecorder.recordChat(ctx, llmClient.defaultModel(), System.currentTimeMillis() - start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message("模型调用失败，请稍后重试")
                    .conversationId(ctx.getConversationId())
                    .build();
        } catch (AiException e) {
            auditRecorder.recordChat(ctx, llmClient.defaultModel(), System.currentTimeMillis() - start, false, e.getMessage());
            return CopilotChatResponse.builder()
                    .type(CopilotResponseType.ERROR)
                    .message(e.getMessage())
                    .conversationId(ctx.getConversationId())
                    .build();
        }
    }
}
