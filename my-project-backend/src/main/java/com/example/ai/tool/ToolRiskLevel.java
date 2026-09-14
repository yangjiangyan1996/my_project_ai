package com.example.ai.tool;

/**
 * Tool risk levels from Frozen Spec.
 * L4 is disabled in V1 and must not be registered.
 */
public enum ToolRiskLevel {
    L0_READ,
    L1_ANALYSIS,
    L2_DRAFT,
    L3_CONFIRM_REQUIRED,
    L4_HIGH_RISK;

    public boolean isDisabledInV1() {
        return this == L4_HIGH_RISK;
    }
}
