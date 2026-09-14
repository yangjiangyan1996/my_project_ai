package com.example.ai.permission;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiPermissionException;
import com.example.ai.exception.AiValidationException;
import com.example.entity.base.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiUserContextTest {

    private final AiUserContext aiUserContext = new AiUserContext();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void fromCurrentUser_usesJwtTenantOnly() {
        UserInfo user = new UserInfo(9L, null, "u", "n", "ADMIN", null, null, 77L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));

        AiExecutionContext ctx = aiUserContext.fromCurrentUser(null, Map.of("page", "index"));
        assertEquals(9L, ctx.getUserId());
        assertEquals(77L, ctx.getTenantId());
        assertNotNull(ctx.getConversationId());
    }

    @Test
    void fromCurrentUser_unauthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AiPermissionException.class,
                () -> aiUserContext.fromCurrentUser("c", Map.of()));
    }

    @Test
    void rejectTenantSpoof() {
        assertThrows(AiValidationException.class,
                () -> aiUserContext.assertTenantNotSpoofed(2L, 1L));
        assertDoesNotThrow(() -> aiUserContext.assertTenantNotSpoofed(null, 1L));
        assertDoesNotThrow(() -> aiUserContext.assertTenantNotSpoofed(1L, 1L));
    }
}
