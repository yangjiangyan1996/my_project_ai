package com.example.ai.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemPromptFactoryTest {

    @Test
    void promptContainsSafetyRules() {
        String p = new SystemPromptFactory().buildWarehouseCopilotPrompt();
        assertTrue(p.contains("不得编造"));
        assertTrue(p.contains("未注册 Tool") || p.contains("未注册"));
        assertTrue(p.contains("其他租户"));
        assertTrue(p.contains("L4"));
        assertTrue(p.contains(SystemPromptFactory.VERSION));
        assertTrue(p.contains("只允许只读") || p.contains("只读查询"));
    }
}
