package com.example.entity.deepseek;

import lombok.Data;

import java.util.List;

/**
 * DeepSeek / OpenAI-compatible chat completions response.
 */
@Data
public class DeepSeekResponse {
    private List<Choice> choices;
    private Usage usage;
    private String model;

    @Data
    public static class Choice {
        private Message message;
        private Integer index;
        private String finish_reason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
        private List<ToolCall> tool_calls;
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

    @Data
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}
