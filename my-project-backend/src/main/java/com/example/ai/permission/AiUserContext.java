package com.example.ai.permission;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiValidationException;
import com.example.entity.base.UserInfo;
import com.example.filter.UserUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Builds {@link AiExecutionContext} from CurrentUser only.
 * Phase H: wire real RBAC into permissions set.
 */
@Component
public class AiUserContext {

    /**
     * Resolve execution context. Ignores any client/LLM supplied tenantId.
     */
    public AiExecutionContext fromCurrentUser(String conversationId, Map<String, Object> requestMetadata) {
        UserInfo user = UserUtil.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new AiPermissionException("未登录，无法使用 AI Copilot");
        }
        if (user.getTenantId() == null) {
            throw new AiPermissionException("当前用户缺少租户信息，无法使用 AI Copilot");
        }
        String cid = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
        return AiExecutionContext.builder()
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .role(user.getRole())
                .conversationId(cid)
                .requestId(UUID.randomUUID().toString())
                .permissions(Collections.emptySet()) // Phase H: fill from role/menu
                .requestMetadata(requestMetadata == null ? Collections.emptyMap() : requestMetadata)
                .build();
    }

    /**
     * Explicitly reject spoofed tenant on request body.
     */
    public void assertTenantNotSpoofed(Long requestTenantId, Long contextTenantId) {
        if (requestTenantId != null && !requestTenantId.equals(contextTenantId)) {
            throw new AiValidationException("禁止通过请求指定或覆盖租户");
        }
    }
}
