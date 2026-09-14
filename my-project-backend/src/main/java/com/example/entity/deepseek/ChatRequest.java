package com.example.entity.deepseek;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek / OpenAI-compatible chat completions request.
 * Provider-specific — must not leak into Orchestrator.
 */
@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;
    private List<Map<String, Object>> tools;
    private Object tool_choice;

    @Data
    public static class Message {
        private String role;
        private String content;
        private String name;
        private String tool_call_id;
        private List<ToolCall> tool_calls;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    public static class ToolCall {
        private String id;
        private String type;
        private FunctionCall function;
    }

    @Data
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
}
