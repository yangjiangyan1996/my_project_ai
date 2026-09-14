package com.example.ai.controller;

import com.example.ai.application.WarehouseAiOrchestrator;
import com.example.ai.exception.AiException;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiValidationException;
import com.example.ai.model.CopilotChatRequest;
import com.example.ai.model.CopilotChatResponse;
import com.example.ai.model.CopilotResponseType;
import com.example.entity.base.RespBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI Copilot HTTP entry. Auth via /api/auth/** security filter.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/ai")
public class AiCopilotController {

    @Resource
    private WarehouseAiOrchestrator warehouseAiOrchestrator;

    @GetMapping("/health")
    public RespBean<Map<String, Object>> health() {
        return RespBean.success(Map.of(
                "status", "UP",
                "module", "ai-wms-copilot",
                "phase", "D"
        ));
    }

    @PostMapping("/chat")
    public RespBean<CopilotChatResponse> chat(@RequestBody CopilotChatRequest request) {
        try {
            return RespBean.success(warehouseAiOrchestrator.chat(request));
        } catch (AiPermissionException e) {
            CopilotChatResponse body = CopilotChatResponse.builder()
                    .type(CopilotResponseType.PERMISSION_DENIED)
                    .message(e.getMessage())
                    .conversationId(request == null ? null : request.getConversationId())
                    .build();
            RespBean<CopilotChatResponse> resp = RespBean.failure(403, e.getMessage());
            resp.setData(body);
            return resp;
        } catch (AiValidationException e) {
            return RespBean.failure(400, e.getMessage());
        } catch (AiException e) {
            log.error("AI Copilot error", e);
            return RespBean.failure(500, e.getMessage());
        }
    }
}
