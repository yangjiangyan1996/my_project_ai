package com.example.ai.tool.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiBusinessException;
import com.example.ai.exception.AiValidationException;
import com.example.entity.base.UserInfo;
import jakarta.validation.ValidationException;

import java.util.List;
import java.util.Map;

/**
 * Shared helpers for Phase C read-only WMS tools.
 */
public final class WmsQuerySupport {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;

    private WmsQuerySupport() {
    }

    public static UserInfo userInfo(AiExecutionContext ctx) {
        return new UserInfo(
                ctx.getUserId(),
                null,
                ctx.getUsername(),
                null,
                ctx.getRole(),
                null,
                null,
                ctx.getTenantId()
        );
    }

    public static int limit(Map<String, Object> args) {
        Object raw = args == null ? null : args.get("limit");
        int limit = DEFAULT_LIMIT;
        if (raw instanceof Number n) {
            limit = n.intValue();
        }
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public static <T> Page<T> page(int limit) {
        return new Page<>(1, limit);
    }

    public static String str(Map<String, Object> args, String key) {
        if (args == null || args.get(key) == null) {
            return null;
        }
        String v = String.valueOf(args.get(key)).trim();
        return v.isEmpty() ? null : v;
    }

    public static Long lng(Map<String, Object> args, String key) {
        if (args == null || args.get(key) == null) {
            return null;
        }
        Object v = args.get(key);
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new AiValidationException("参数不是合法整数: " + key);
        }
    }

    public static Integer integer(Map<String, Object> args, String key) {
        Long v = lng(args, key);
        return v == null ? null : v.intValue();
    }

    public static RuntimeException wrapFacade(RuntimeException e) {
        if (e instanceof ValidationException || e instanceof AiValidationException || e instanceof AiBusinessException) {
            return e;
        }
        throw e;
    }

    public static boolean looksNotFound(Throwable e) {
        String m = e.getMessage();
        return m != null && (m.contains("不存在") || m.contains("未找到"));
    }

    public static Map<String, Object> listPayload(List<?> items, long total) {
        boolean hasMore = total > items.size();
        return Map.of(
                "items", items,
                "total", total,
                "hasMore", hasMore
        );
    }
}
