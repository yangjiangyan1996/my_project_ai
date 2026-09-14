package com.example.ai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopilotResponseContractTest {

    @Test
    void responseTypesCoverFrozenSpec() {
        assertNotNull(CopilotResponseType.valueOf("TEXT"));
        assertNotNull(CopilotResponseType.valueOf("TOOL_RESULT"));
        assertNotNull(CopilotResponseType.valueOf("DRAFT"));
        assertNotNull(CopilotResponseType.valueOf("ERROR"));
        assertNotNull(CopilotResponseType.valueOf("PERMISSION_DENIED"));
        assertNotNull(CopilotResponseType.valueOf("NEED_CLARIFICATION"));
        assertEquals(6, CopilotResponseType.values().length);
    }

    @Test
    void chatResponseHasStructuredFields() {
        CopilotChatResponse r = CopilotChatResponse.builder()
                .type(CopilotResponseType.TEXT)
                .message("hi")
                .conversationId("c")
                .build();
        assertNotNull(r.getToolCalls());
        assertNotNull(r.getCards());
        assertNotNull(r.getActions());
        assertEquals(CopilotResponseType.TEXT, r.getType());
    }
}
