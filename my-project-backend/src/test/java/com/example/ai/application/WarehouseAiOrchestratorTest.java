package com.example.ai.application;

import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.conversation.InMemoryConversationStore;
import com.example.ai.llm.FakeLlmClient;
import com.example.ai.model.CopilotChatRequest;
import com.example.ai.model.CopilotChatResponse;
import com.example.ai.model.CopilotResponseType;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiUserContext;
import com.example.ai.prompt.SystemPromptFactory;
import com.example.ai.tool.ToolRegistry;
import com.example.entity.base.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseAiOrchestratorTest {

    private WarehouseAiOrchestrator orchestrator;
    private FakeLlmClient fakeLlmClient;
    private InMemoryConversationStore conversationStore;

    @BeforeEach
    void setUp() {
        fakeLlmClient = new FakeLlmClient("Phase A foundation OK");
        conversationStore = new InMemoryConversationStore();
        orchestrator = new WarehouseAiOrchestrator();
        ReflectionTestUtils.setField(orchestrator, "llmClient", fakeLlmClient);
        ReflectionTestUtils.setField(orchestrator, "aiUserContext", new AiUserContext());
        ReflectionTestUtils.setField(orchestrator, "permissionChecker", new AiPermissionChecker());
        ReflectionTestUtils.setField(orchestrator, "systemPromptFactory", new SystemPromptFactory());
        ReflectionTestUtils.setField(orchestrator, "conversationStore", conversationStore);
        ReflectionTestUtils.setField(orchestrator, "auditRecorder", new LoggingAiAuditRecorder());
        ReflectionTestUtils.setField(orchestrator, "toolRegistry", new ToolRegistry());

        UserInfo user = new UserInfo(1L, null, "admin", "a", "ADMIN", null, null, 100L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chat_returnsStructuredText_andDoesNotBindDeepSeek() {
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("hello")
                .tenantId(999L) // spoof attempt
                .build());
        // spoof rejected before LLM
        // wait - assertTenantNotSpoofed will throw and be caught as AiException -> ERROR
        assertEquals(CopilotResponseType.ERROR, resp.getType());
        assertTrue(resp.getMessage().contains("租户"));
        assertEquals(0, fakeLlmClient.getCallCount());
    }

    @Test
    void chat_success_withoutTenantSpoof() {
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("今天待办?")
                .conversationId("conv-1")
                .build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals("Phase A foundation OK", resp.getMessage());
        assertEquals("conv-1", resp.getConversationId());
        assertEquals(1, fakeLlmClient.getCallCount());
        assertEquals(2, conversationStore.list("conv-1", 100L).size());
    }

    @Test
    void orchestratorDependsOnLlmClientAbstraction() {
        assertEquals("fake", fakeLlmClient.providerId());
        assertNotEquals("deepseek", fakeLlmClient.providerId());
    }
}
