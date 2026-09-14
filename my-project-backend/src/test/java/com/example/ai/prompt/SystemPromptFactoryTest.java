package com.example.ai.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemPromptFactoryTest {

    @Test
    void promptContainsSafetyRules() {
        String p = new SystemPromptFactory().buildWarehouseCopilotPrompt();
        assertTrue(p.contains("不得编造库存"));
        assertTrue(p.contains("不得绕过 Tool"));
        assertTrue(p.contains("其他租户"));
        assertTrue(p.contains("L4"));
        assertTrue(p.contains(SystemPromptFactory.VERSION));
    }
}
