package com.example.ai.application;

import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.conversation.InMemoryConversationStore;
import com.example.ai.llm.FakeLlmClient;
import com.example.ai.llm.LlmChatResult;
import com.example.ai.llm.LlmToolCall;
import com.example.ai.model.CopilotChatRequest;
import com.example.ai.model.CopilotChatResponse;
import com.example.ai.model.CopilotResponseType;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiToolPermissionGuard;
import com.example.ai.permission.AiUserContext;
import com.example.ai.prompt.SystemPromptFactory;
import com.example.ai.tool.FakeToolFixtures;
import com.example.ai.tool.LlmToolSchemaAdapter;
import com.example.ai.tool.ToolCallGuard;
import com.example.ai.tool.ToolDefinition;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolExecutor;
import com.example.ai.tool.ToolInputValidator;
import com.example.ai.tool.ToolRegistry;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.ToolResultContextCompressor;
import com.example.ai.tool.ToolRiskLevel;
import com.example.ai.tool.ToolRiskPolicy;
import com.example.entity.base.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase D — orchestrator tool-calling loop (mocked LLM, no DeepSeek / no DEV_DB).
 */
class WarehouseAiOrchestratorPhaseDTest {

    private WarehouseAiOrchestrator orchestrator;
    private FakeLlmClient fakeLlm;
    private InMemoryConversationStore conversationStore;
    private ToolRegistry registry;
    private ToolExecutor executor;
    private ToolCallGuard callGuard;
    private AtomicInteger inventoryHits;

    @BeforeEach
    void setUp() {
        conversationStore = new InMemoryConversationStore();
        ReflectionTestUtils.setField(conversationStore, "maxMessagesPerConversation", 40);
        ReflectionTestUtils.setField(conversationStore, "maxSessions", 500);
        ReflectionTestUtils.setField(conversationStore, "ttlHours", 24);

        registry = new ToolRegistry();
        registry.register(FakeToolFixtures.readTool());
        registry.register(searchWarehouseTool());
        registry.register(searchProductTool());
        inventoryHits = new AtomicInteger();
        registry.register(getInventoryTool());

        callGuard = new ToolCallGuard();
        ReflectionTestUtils.setField(callGuard, "maxToolCalls", 6);
        ReflectionTestUtils.setField(callGuard, "maxSameToolCalls", 2);
        ReflectionTestUtils.setField(callGuard, "objectMapper", new ObjectMapper());

        AiToolPermissionGuard permGuard = new AiToolPermissionGuard();
        ReflectionTestUtils.setField(permGuard, "permissionChecker", new AiPermissionChecker());

        executor = new ToolExecutor();
        ReflectionTestUtils.setField(executor, "toolRegistry", registry);
        ReflectionTestUtils.setField(executor, "toolRiskPolicy", new ToolRiskPolicy());
        ReflectionTestUtils.setField(executor, "permissionGuard", permGuard);
        ReflectionTestUtils.setField(executor, "inputValidator", new ToolInputValidator());
        ReflectionTestUtils.setField(executor, "auditRecorder", new LoggingAiAuditRecorder());

        ToolResultContextCompressor compressor = new ToolResultContextCompressor();
        ReflectionTestUtils.setField(compressor, "objectMapper", new ObjectMapper());

        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        orchestrator = new WarehouseAiOrchestrator();
        ReflectionTestUtils.setField(orchestrator, "aiUserContext", new AiUserContext());
        ReflectionTestUtils.setField(orchestrator, "permissionChecker", new AiPermissionChecker());
        ReflectionTestUtils.setField(orchestrator, "systemPromptFactory", new SystemPromptFactory());
        ReflectionTestUtils.setField(orchestrator, "conversationStore", conversationStore);
        ReflectionTestUtils.setField(orchestrator, "auditRecorder", new LoggingAiAuditRecorder());
        ReflectionTestUtils.setField(orchestrator, "toolRegistry", registry);
        ReflectionTestUtils.setField(orchestrator, "toolExecutor", executor);
        ReflectionTestUtils.setField(orchestrator, "toolCallGuard", callGuard);
        ReflectionTestUtils.setField(orchestrator, "toolSchemaAdapter", new LlmToolSchemaAdapter());
        ReflectionTestUtils.setField(orchestrator, "resultCompressor", compressor);
        ReflectionTestUtils.setField(orchestrator, "environment", env);

        UserInfo user = new UserInfo(1L, null, "admin", "a", "ADMIN", null, null, 100L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void wireLlm(FakeLlmClient client) {
        this.fakeLlm = client;
        ReflectionTestUtils.setField(orchestrator, "llmClient", client);
    }

    @Test
    void case1_noTool_directFinal() {
        wireLlm(new FakeLlmClient("你好，我是 WMS Copilot"));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("你好").conversationId("d1").build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals(1, fakeLlm.getCallCount());
        assertEquals(0, resp.getUsage().getTotalToolCalls());
    }

    @Test
    void case2_singleTool_thenFinal() {
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("c1", "search_warehouse", Map.of("keyword", "仓")),
                req -> FakeLlmClient.text("当前有杭州仓、上海仓")
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("有哪些仓库？").conversationId("d2").build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals(2, fakeLlm.getCallCount());
        assertEquals(1, resp.getUsage().getTotalToolCalls());
        assertEquals(List.of("search_warehouse"), resp.getDebugToolTrace());
    }

    @Test
    void case3_multiTool_entityResolution() {
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("t1", "search_product", Map.of("keyword", "A001")),
                req -> FakeLlmClient.toolCall("t2", "search_warehouse", Map.of("keyword", "杭州")),
                req -> FakeLlmClient.toolCall("t3", "get_inventory", Map.of("productId", 10, "warehouseId", 1)),
                req -> FakeLlmClient.text("A001 在杭州仓剩余 520")
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("A001 在杭州仓库存多少？").conversationId("d3").build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals(4, fakeLlm.getCallCount());
        assertEquals(3, resp.getUsage().getTotalToolCalls());
        assertEquals(1, inventoryHits.get());
        assertEquals(List.of("search_product", "search_warehouse", "get_inventory"), resp.getDebugToolTrace());
    }

    @Test
    void case4_unknownTool_blockedThenCanFinalize() {
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("x", "not_a_real_tool", Map.of()),
                req -> FakeLlmClient.text("该能力不可用")
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("hack").conversationId("d4").build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals(1, resp.getToolCalls().size());
        assertFalse(resp.getToolCalls().get(0).isSuccess());
        assertEquals(ToolErrorCode.NOT_FOUND.name(), resp.getToolCalls().get(0).getErrorCode());
    }

    @Test
    void case5_invalidArgument_validationError() {
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("v", "fake_read", Map.of()),
                req -> FakeLlmClient.text("参数不足，请提供关键字")
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("查一下").conversationId("d5").build());
        assertEquals(ToolErrorCode.VALIDATION_ERROR.name(), resp.getToolCalls().get(0).getErrorCode());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
    }

    @Test
    void case6_permissionDenied_stops() {
        registry.register(ToolDefinition.builder()
                .name("secret_read")
                .description("needs perm")
                .riskLevel(ToolRiskLevel.L0_READ)
                .permission("ck:secret:list")
                .inputSchema(Map.of("type", "object", "properties", Map.of()))
                .handler((args, ctx) -> ToolResult.ok(Map.of(), "should-not-run"))
                .build());
        // Force permission set non-empty without the required code
        // Rebuild context path: AiUserContext uses CurrentUser permissions - empty by default.
        // Simulate via custom tool that returns PERMISSION_DENIED from executor by setting permissions on a wrapper —
        // Use AiPermissionChecker with permissions on execution context via a tool that checks... 
        // Easier: register tool and patch chat by using executor with user permissions.
        // AiUserContext.fromCurrentUser leaves permissions empty → interim allow.
        // So inject a tool handler that is never reached; instead call orchestrator with a fake tool
        // that ToolExecutor rejects: set permissions on Security user... AiUserContext may not load them.
        // Direct approach: tool that always fails permission inside handler won't map to PERMISSION_DENIED.
        // Use ToolExecutor path: non-empty permissions without required.
        // Patch: create execution by overriding AiUserContext - too heavy.
        // Register tool and use Reflection to set permissions after fromCurrentUser — can't.
        // Implement tiny stub AiUserContext? 
        // Simplest: custom Fake that returns PERMISSION_DENIED from execute without going through guard —
        // but orchestrator uses ToolExecutor.
        // Wire a custom AiUserContext subclass via anonymous... it's a @Component class.
        // Change approach: use ToolDefinition with permission ck:ai:test and set context permissions to {other}.
        // Looking at AiUserContext...
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("p", "secret_read", Map.of())
        ));
        // Manually set permissions on a wrapped orchestrator call by replacing AiUserContext
        AiUserContext stubCtx = new AiUserContext() {
            @Override
            public com.example.ai.context.AiExecutionContext fromCurrentUser(String conversationId,
                                                                              Map<String, Object> requestMetadata) {
                com.example.ai.context.AiExecutionContext base = super.fromCurrentUser(conversationId, requestMetadata);
                return com.example.ai.context.AiExecutionContext.builder()
                        .userId(base.getUserId())
                        .tenantId(base.getTenantId())
                        .username(base.getUsername())
                        .role(base.getRole())
                        .conversationId(base.getConversationId())
                        .requestId(base.getRequestId())
                        .permissions(Set.of("ck:other:list"))
                        .requestMetadata(base.getRequestMetadata())
                        .build();
            }
        };
        ReflectionTestUtils.setField(orchestrator, "aiUserContext", stubCtx);

        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("secret").conversationId("d6").build());
        assertEquals(CopilotResponseType.PERMISSION_DENIED, resp.getType());
        assertTrue(resp.getMessage().contains("权限"));
    }

    @Test
    void case7_l4CannotRegister_unknownHighRiskRejectedByRegistry() {
        assertThrows(Exception.class, () -> registry.register(ToolDefinition.builder()
                .name("approve_evil")
                .description("l4")
                .riskLevel(ToolRiskLevel.L4_HIGH_RISK)
                .permission("ck:ai:test")
                .handler((a, c) -> ToolResult.ok(Map.of(), "no"))
                .build()));
    }

    @Test
    void case8_toolTimeout_mapped_noFabricationRequired() {
        registry.register(ToolDefinition.builder()
                .name("slow_tool")
                .description("slow")
                .riskLevel(ToolRiskLevel.L0_READ)
                .permission("ck:ai:test")
                .timeoutMs(30)
                .inputSchema(Map.of("type", "object", "properties", Map.of()))
                .handler((args, ctx) -> {
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return ToolResult.ok(Map.of("x", 1), "late");
                })
                .build());
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("s", "slow_tool", Map.of()),
                req -> FakeLlmClient.text("当前查询失败，请稍后重试")
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("慢查询").conversationId("d8").build());
        assertEquals(ToolErrorCode.TIMEOUT.name(), resp.getToolCalls().get(0).getErrorCode());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertTrue(resp.getMessage().contains("失败") || resp.getMessage().contains("重试"));
    }

    @Test
    void case9_loopDetection() {
        ReflectionTestUtils.setField(callGuard, "maxSameToolCalls", 2);
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("a", "search_product", Map.of("keyword", "A001")),
                req -> FakeLlmClient.toolCall("b", "search_product", Map.of("keyword", "A001")),
                req -> FakeLlmClient.toolCall("c", "search_product", Map.of("keyword", "A001"))
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("循环").conversationId("d9").build());
        assertEquals(CopilotResponseType.NEED_CLARIFICATION, resp.getType());
        assertEquals(2, resp.getUsage().getTotalToolCalls());
    }

    @Test
    void case10_maxToolCalls() {
        ReflectionTestUtils.setField(callGuard, "maxToolCalls", 2);
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("a", "search_product", Map.of("keyword", "1")),
                req -> FakeLlmClient.toolCall("b", "search_warehouse", Map.of("keyword", "2")),
                req -> FakeLlmClient.toolCall("c", "get_inventory", Map.of("productId", 1))
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("超限").conversationId("d10").build());
        assertEquals(CopilotResponseType.ERROR, resp.getType());
        assertTrue(resp.getMessage().contains("上限") || resp.getMessage().contains("次数"));
        assertEquals(2, resp.getUsage().getTotalToolCalls());
    }

    @Test
    void case11_ambiguous_needClarification() {
        registry.register(ToolDefinition.builder()
                .name("search_ambiguous")
                .description("multi")
                .riskLevel(ToolRiskLevel.L0_READ)
                .permission("ck:ai:test")
                .inputSchema(Map.of("type", "object", "properties", Map.of()))
                .handler((args, ctx) -> ToolResult.ok(Map.of(
                        "ambiguous", true,
                        "items", List.of(
                                Map.of("warehouseId", 1, "name", "杭州一号仓"),
                                Map.of("warehouseId", 2, "name", "杭州备用仓")
                        ),
                        "total", 2
                ), "多结果"))
                .build());
        wireLlm(FakeLlmClient.scripted(
                req -> FakeLlmClient.toolCall("m", "search_ambiguous", Map.of())
        ));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("杭州仓").conversationId("d11").build());
        assertEquals(CopilotResponseType.NEED_CLARIFICATION, resp.getType());
        assertFalse(resp.getCards().isEmpty());
        assertEquals(1, fakeLlm.getCallCount());
    }

    @Test
    void case12_multiTurn_usesPriorAssistantContext() {
        wireLlm(new FakeLlmClient("杭州仓 520，上海仓 300"));
        orchestrator.chat(CopilotChatRequest.builder()
                .message("A001库存多少？").conversationId("d12").build());

        AtomicInteger sawPrior = new AtomicInteger();
        wireLlm(FakeLlmClient.scripted(req -> {
            boolean hasPrior = req.getMessages().stream()
                    .anyMatch(m -> m.getContent() != null && m.getContent().contains("杭州仓"));
            if (hasPrior) {
                sawPrior.incrementAndGet();
            }
            return FakeLlmClient.text("杭州仓还有 520");
        }));
        CopilotChatResponse resp = orchestrator.chat(CopilotChatRequest.builder()
                .message("杭州仓呢？").conversationId("d12").build());
        assertEquals(CopilotResponseType.TEXT, resp.getType());
        assertEquals(1, sawPrior.get());
    }

    private com.example.ai.tool.AiTool searchWarehouseTool() {
        return new com.example.ai.tool.AiTool() {
            @Override public String name() { return "search_warehouse"; }
            @Override public String description() { return "search wh"; }
            @Override public ToolRiskLevel riskLevel() { return ToolRiskLevel.L0_READ; }
            @Override public String requiredPermission() { return "ck:ai:test"; }
            @Override public Map<String, Object> inputSchema() {
                return Map.of("type", "object", "properties", Map.of("keyword", Map.of("type", "string")));
            }
            @Override public ToolResult execute(Map<String, Object> arguments, com.example.ai.context.AiExecutionContext context) {
                return ToolResult.ok(Map.of("items", List.of(Map.of("warehouseId", 1, "name", "杭州仓")),
                        "total", 1, "ambiguous", false), "ok");
            }
        };
    }

    private com.example.ai.tool.AiTool searchProductTool() {
        return new com.example.ai.tool.AiTool() {
            @Override public String name() { return "search_product"; }
            @Override public String description() { return "search p"; }
            @Override public ToolRiskLevel riskLevel() { return ToolRiskLevel.L0_READ; }
            @Override public String requiredPermission() { return "ck:ai:test"; }
            @Override public Map<String, Object> inputSchema() {
                return Map.of("type", "object", "properties", Map.of("keyword", Map.of("type", "string")));
            }
            @Override public ToolResult execute(Map<String, Object> arguments, com.example.ai.context.AiExecutionContext context) {
                return ToolResult.ok(Map.of("items", List.of(Map.of("productId", 10, "sku", "A001")),
                        "total", 1, "ambiguous", false), "ok");
            }
        };
    }

    private com.example.ai.tool.AiTool getInventoryTool() {
        return new com.example.ai.tool.AiTool() {
            @Override public String name() { return "get_inventory"; }
            @Override public String description() { return "inv"; }
            @Override public ToolRiskLevel riskLevel() { return ToolRiskLevel.L0_READ; }
            @Override public String requiredPermission() { return "ck:ai:test"; }
            @Override public Map<String, Object> inputSchema() {
                return Map.of("type", "object", "properties", Map.of(
                        "productId", Map.of("type", "integer"),
                        "warehouseId", Map.of("type", "integer")));
            }
            @Override public ToolResult execute(Map<String, Object> arguments, com.example.ai.context.AiExecutionContext context) {
                inventoryHits.incrementAndGet();
                return ToolResult.ok(Map.of("items", List.of(Map.of("quantity", 520))), "ok");
            }
        };
    }
}
