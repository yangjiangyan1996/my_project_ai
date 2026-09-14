package com.example.ai.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    private String conversationId;
    private String messageId;
    private String role;
    private String content;
    private Instant createdAt;
    private Long userId;
    private Long tenantId;
}
