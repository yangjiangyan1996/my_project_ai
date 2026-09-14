package com.example.ai.tool;

import com.example.ai.context.AiExecutionContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test-only fake tools. Must never be Spring @Component in main.
 */
public final class FakeToolFixtures {

    private FakeToolFixtures() {
    }

    public static AiTool readTool() {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_read";
            }

            @Override
            public String description() {
                return "fake L0 read";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L0_READ;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public Map<String, Object> inputSchema() {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("q", Map.of("type", "string"));
                return Map.of(
                        "type", "object",
                        "required", List.of("q"),
                        "properties", props
                );
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                return ToolResult.ok(Map.of(
                        "q", arguments.get("q"),
                        "tenantId", context.getTenantId()
                ), "read-ok");
            }
        };
    }

    public static AiTool analysisTool() {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_analysis";
            }

            @Override
            public String description() {
                return "fake L1";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L1_ANALYSIS;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                return ToolResult.ok(Map.of("score", 1), "analysis-ok");
            }
        };
    }

    public static AiTool draftTool() {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_draft";
            }

            @Override
            public String description() {
                return "fake L2 draft";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L2_DRAFT;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                return ToolResult.ok(Map.of("draftId", "d-1"), "draft-ok");
            }
        };
    }

    public static AiTool confirmTool() {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_confirm";
            }

            @Override
            public String description() {
                return "fake L3";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L3_CONFIRM_REQUIRED;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                return ToolResult.ok(Map.of("created", true), "confirm-ok");
            }
        };
    }

    public static AiTool highRiskTool() {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_high_risk";
            }

            @Override
            public String description() {
                return "fake L4";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L4_HIGH_RISK;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                return ToolResult.ok(Map.of(), "should-not-run");
            }
        };
    }

    public static AiTool slowTool(long sleepMs) {
        return new AiTool() {
            @Override
            public String name() {
                return "fake_slow";
            }

            @Override
            public String description() {
                return "timeout test";
            }

            @Override
            public ToolRiskLevel riskLevel() {
                return ToolRiskLevel.L0_READ;
            }

            @Override
            public String requiredPermission() {
                return "ck:ai:test";
            }

            @Override
            public long timeoutMs() {
                return 50L;
            }

            @Override
            public ToolResult execute(Map<String, Object> arguments, AiExecutionContext context) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return ToolResult.ok(Map.of(), "late");
            }
        };
    }
}
