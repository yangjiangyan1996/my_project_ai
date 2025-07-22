package com.example.filter;

import com.example.config.UserNotLoggedInException;
import com.example.entity.base.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户信息
 */
public class UserUtil {

    public static UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserInfo) {
                return (UserInfo) principal;
            }
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUsername() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}
