package com.example.ai.tool;

import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiToolPermissionGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase B framework tests — no real WMS tools.
 */
class ToolFrameworkPhaseBTest {

    private ToolRegistry registry;
    private ToolExecutor executor;
    private ToolRiskPolicy riskPolicy;
    private LlmToolSchemaAdapter schemaAdapter;
    private ToolCallGuard callGuard;
    private AtomicInteger auditCount;
    private AiExecutionContext ctx;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        riskPolicy = new ToolRiskPolicy();
        schemaAdapter = new LlmToolSchemaAdapter();
        callGuard = new ToolCallGuard();
        ReflectionTestUtils.setField(callGuard, "maxToolCalls", 3);

        auditCount = new AtomicInteger();
        LoggingAiAuditRecorder audit = new LoggingAiAuditRecorder() {
            @Override
            public void recordToolCall(AiExecutionContext c, String toolName, String riskLevel,
                                       boolean success, long latencyMs, String errorSummary) {
                auditCount.incrementAndGet();
                super.recordToolCall(c, toolName, riskLevel, success, latencyMs, errorSummary);
            }
        };

        AiToolPermissionGuard guard = new AiToolPermissionGuard();
        ReflectionTestUtils.setField(guard, "permissionChecker", new AiPermissionChecker());

        executor = new ToolExecutor();
        ReflectionTestUtils.setField(executor, "toolRegistry", registry);
        ReflectionTestUtils.setField(executor, "toolRiskPolicy", riskPolicy);
        ReflectionTestUtils.setField(executor, "permissionGuard", guard);
        ReflectionTestUtils.setField(executor, "inputValidator", new ToolInputValidator());
        ReflectionTestUtils.setField(executor, "auditRecorder", audit);

        ctx = AiExecutionContext.builder()
                .userId(1L)
                .tenantId(88L)
                .conversationId("c")
                .requestId("r")
                .permissions(Set.of())
                .build();
    }

    @Test
    void registerAiToolAndDuplicateFails() {
        registry.register(FakeToolFixtures.readTool());
        assertThrows(AiValidationException.class, () -> registry.register(FakeToolFixtures.readTool()));
    }

    @Test
    void unknownToolMappedToNotFound() {
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("nope")
                .executionContext(ctx)
                .build());
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.NOT_FOUND.name(), r.getErrorCode());
    }

    @Test
    void inputValidationRequired() {
        registry.register(FakeToolFixtures.readTool());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_read")
                .arguments(Map.of())
                .executionContext(ctx)
                .build());
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.VALIDATION_ERROR.name(), r.getErrorCode());
    }

    @Test
    void l0AllowWithTenantFromContext() {
        registry.register(FakeToolFixtures.readTool());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_read")
                .arguments(Map.of("q", "A001", "tenantId", 999L))
                .executionContext(ctx)
                .build());
        assertTrue(r.isSuccess());
        assertEquals(ToolErrorCode.SUCCESS.name(), r.getErrorCode());
        assertEquals(88L, r.getData().get("tenantId"));
        assertEquals(1, auditCount.get());
    }

    @Test
    void l1Allow() {
        registry.register(FakeToolFixtures.analysisTool());
        assertTrue(executor.execute(ToolRequest.builder()
                .toolName("fake_analysis")
                .executionContext(ctx)
                .build()).isSuccess());
    }

    @Test
    void l2DraftBoundaryAllowExecute() {
        registry.register(FakeToolFixtures.draftTool());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_draft")
                .executionContext(ctx)
                .build());
        assertTrue(r.isSuccess());
        assertEquals("d-1", r.getData().get("draftId"));
    }

    @Test
    void l3WithoutConfirmDenied() {
        registry.register(FakeToolFixtures.confirmTool());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_confirm")
                .executionContext(ctx)
                .build());
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.PERMISSION_DENIED.name(), r.getErrorCode());
    }

    @Test
    void l3WithConfirmOk() {
        registry.register(FakeToolFixtures.confirmTool());
        AiExecutionContext confirmed = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).conversationId("c").requestId("r2")
                .confirmToken("tok").build();
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_confirm")
                .confirmToken("tok")
                .executionContext(confirmed)
                .build());
        assertTrue(r.isSuccess());
    }

    @Test
    void l4AlwaysDenyViaRiskPolicy() {
        assertThrows(AiValidationException.class,
                () -> registry.register(FakeToolFixtures.highRiskTool()));
        assertThrows(com.example.ai.exception.AiPermissionException.class,
                () -> riskPolicy.assertExecutable(FakeToolFixtures.highRiskTool().toDefinition(), ctx, null));
    }

    @Test
    void permissionDeniedWhenMapped() {
        registry.register(FakeToolFixtures.readTool());
        AiExecutionContext limited = AiExecutionContext.builder()
                .userId(1L).tenantId(88L).conversationId("c").requestId("r")
                .permissions(Set.of("ck:other")).build();
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_read")
                .arguments(Map.of("q", "x"))
                .executionContext(limited)
                .build());
        assertEquals(ToolErrorCode.PERMISSION_DENIED.name(), r.getErrorCode());
    }

    @Test
    void timeoutMapped() {
        registry.register(FakeToolFixtures.slowTool(500));
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("fake_slow")
                .executionContext(ctx)
                .build());
        assertEquals(ToolErrorCode.TIMEOUT.name(), r.getErrorCode());
    }

    @Test
    void schemaAdapterProducesFunctionShape() {
        registry.register(FakeToolFixtures.readTool());
        Map<String, Object> schema = schemaAdapter.toFunctionSchema(registry.get("fake_read"));
        assertEquals("function", schema.get("type"));
        assertTrue(schema.containsKey("function"));
        @SuppressWarnings("unchecked")
        Map<String, Object> fn = (Map<String, Object>) schema.get("function");
        assertEquals("fake_read", fn.get("name"));
        List<Map<String, Object>> all = schemaAdapter.toFunctionSchemas(registry.listAll());
        assertEquals(1, all.size());
    }

    @Test
    void toolCallGuardLimit() {
        callGuard.assertWithinLimit(0);
        callGuard.assertWithinLimit(2);
        assertThrows(AiValidationException.class, () -> callGuard.assertWithinLimit(3));
    }

    @Test
    void fakeToolsNotInEmptyProductionRegistry() {
        assertTrue(new ToolRegistry().listAll().isEmpty());
    }

    @Test
    void systemErrorDoesNotExposeStackAsBusinessFact() {
        registry.register(ToolDefinition.builder()
                .name("boom")
                .description("x")
                .riskLevel(ToolRiskLevel.L0_READ)
                .permission("ck:ai:test")
                .handler((a, c) -> {
                    throw new RuntimeException("secret-db-url-password");
                })
                .build());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("boom")
                .executionContext(ctx)
                .build());
        assertEquals(ToolErrorCode.SYSTEM_ERROR.name(), r.getErrorCode());
        assertFalse(r.getMessage().contains("password"));
        assertEquals("系统异常，请稍后重试", r.getMessage());
    }
}
