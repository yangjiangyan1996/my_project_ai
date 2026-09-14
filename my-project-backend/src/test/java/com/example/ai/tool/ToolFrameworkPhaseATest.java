package com.example.ai.tool;

import com.example.ai.audit.LoggingAiAuditRecorder;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiToolException;
import com.example.ai.exception.AiValidationException;
import com.example.ai.permission.AiPermissionChecker;
import com.example.ai.permission.AiToolPermissionGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ToolFrameworkPhaseATest {

    private ToolRegistry registry;
    private ToolRiskPolicy riskPolicy;
    private ToolExecutor executor;
    private AiExecutionContext ctx;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        riskPolicy = new ToolRiskPolicy();
        AiToolPermissionGuard guard = new AiToolPermissionGuard();
        ReflectionTestUtils.setField(guard, "permissionChecker", new AiPermissionChecker());

        executor = new ToolExecutor();
        ReflectionTestUtils.setField(executor, "toolRegistry", registry);
        ReflectionTestUtils.setField(executor, "toolRiskPolicy", riskPolicy);
        ReflectionTestUtils.setField(executor, "permissionGuard", guard);
        ReflectionTestUtils.setField(executor, "inputValidator", new ToolInputValidator());
        ReflectionTestUtils.setField(executor, "auditRecorder", new LoggingAiAuditRecorder());

        ctx = AiExecutionContext.builder()
                .userId(1L)
                .tenantId(10L)
                .username("u")
                .role("ADMIN")
                .conversationId("c1")
                .requestId("r1")
                .permissions(Set.of())
                .build();
    }

    @Test
    void registerAndGet() {
        registry.register(TestEchoToolFactory.createEchoL0());
        assertTrue(registry.contains("test_echo"));
        assertEquals("test_echo", registry.getRequired("test_echo").getName());
        assertFalse(registry.toLlmToolMetadata().isEmpty());
    }

    @Test
    void rejectDuplicateName() {
        registry.register(TestEchoToolFactory.createEchoL0());
        assertThrows(AiValidationException.class,
                () -> registry.register(TestEchoToolFactory.createEchoL0()));
    }

    @Test
    void rejectUnknownTool() {
        assertThrows(AiToolException.class, () -> registry.getRequired("nope"));
    }

    @Test
    void rejectL4Registration() {
        assertThrows(AiValidationException.class,
                () -> registry.register(TestEchoToolFactory.createL4Illegal()));
    }

    @Test
    void l0Allowed() {
        registry.register(TestEchoToolFactory.createEchoL0());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("test_echo")
                .arguments(Map.of("text", "hi", "tenantId", 999L))
                .executionContext(ctx)
                .build());
        assertTrue(r.isSuccess());
        assertEquals("hi", r.getData().get("echo"));
        assertEquals(10L, r.getData().get("tenantId"));
    }

    @Test
    void l3RequiresConfirm() {
        registry.register(TestEchoToolFactory.createL3Confirm());
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("test_confirm_action")
                .executionContext(ctx)
                .build());
        assertFalse(r.isSuccess());
        assertEquals(ToolErrorCode.PERMISSION_DENIED.name(), r.getErrorCode());
    }

    @Test
    void l3WithConfirmOk() {
        registry.register(TestEchoToolFactory.createL3Confirm());
        AiExecutionContext confirmed = AiExecutionContext.builder()
                .userId(1L)
                .tenantId(10L)
                .conversationId("c1")
                .requestId("r2")
                .confirmToken("tok-1")
                .build();
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("test_confirm_action")
                .executionContext(confirmed)
                .confirmToken("tok-1")
                .build());
        assertTrue(r.isSuccess());
    }

    @Test
    void l4RiskPolicyRejectsEvenIfForced() {
        ToolDefinition l4 = TestEchoToolFactory.createL4Illegal();
        assertThrows(com.example.ai.exception.AiPermissionException.class,
                () -> riskPolicy.assertExecutable(l4, ctx, null));
    }

    @Test
    void permissionDeniedWhenMapPresent() {
        registry.register(TestEchoToolFactory.createEchoL0());
        AiExecutionContext limited = AiExecutionContext.builder()
                .userId(1L)
                .tenantId(10L)
                .conversationId("c1")
                .requestId("r3")
                .permissions(Set.of("ck:other"))
                .build();
        ToolResult r = executor.execute(ToolRequest.builder()
                .toolName("test_echo")
                .executionContext(limited)
                .build());
        assertEquals(ToolErrorCode.PERMISSION_DENIED.name(), r.getErrorCode());
    }
}
