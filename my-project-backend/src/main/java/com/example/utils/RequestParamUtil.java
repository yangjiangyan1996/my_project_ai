package com.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class RequestParamUtil {

    private final ObjectMapper objectMapper;

    public RequestParamUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 获取 ObjectMapper 实例
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    /**
     * 将方法参数转换为JSON字符串
     */
    public String convertParamsToJson(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        try {
            // 获取参数名（需要编译时加入-parameters参数）
            String[] paramNames = getParameterNames(method);

            ObjectNode jsonNode = objectMapper.createObjectNode();

            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) continue;

                String paramName = i < paramNames.length ? paramNames[i] : "arg" + i;

                // 过滤掉特定类型的参数
                if (shouldFilterParam(args[i])) {
                    continue;
                }

                // 处理不同类型的参数
                if (args[i] instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) args[i];
                    jsonNode.put(paramName, file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                } else if (isSimpleType(args[i])) {
                    jsonNode.putPOJO(paramName, args[i]);
                } else {
                    try {
                        // 尝试将复杂对象转为JSON
                        String json = objectMapper.writeValueAsString(args[i]);
                        jsonNode.set(paramName, objectMapper.readTree(json));
                    } catch (Exception e) {
                        // 如果序列化失败，使用toString
                        jsonNode.put(paramName, args[i].toString());
                    }
                }
            }

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.warn("转换请求参数为JSON失败", e);
            // 如果转换失败，返回简化版本
            return getSimpleParams(args);
        }
    }

    /**
     * 获取参数名（如果编译时没有-parameters参数，返回arg0, arg1...）
     */
    private String[] getParameterNames(Method method) {
        List<String> names = new ArrayList<>();

        // 方式2：反射获取
        for (int i = 0; i < method.getParameters().length; i++) {
            String name = method.getParameters()[i].getName();
            if (name != null && !name.startsWith("arg")) {
                names.add(name);
            } else {
                names.add("arg" + i);
            }
        }

        return names.toArray(new String[0]);
    }

    /**
     * 判断是否需要过滤该参数
     */
    private boolean shouldFilterParam(Object param) {
        return param instanceof HttpServletRequest ||
                param instanceof HttpServletResponse ||
                param instanceof MultipartHttpServletRequest ||
                param instanceof jakarta.servlet.ServletRequest ||
                param instanceof jakarta.servlet.ServletResponse;
    }

    /**
     * 判断是否是简单类型
     */
    private boolean isSimpleType(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj instanceof Date ||
                obj instanceof Enum ||
                obj.getClass().isPrimitive();
    }

    /**
     * 获取简化的参数信息
     */
    private String getSimpleParams(Object[] args) {
        try {
            Map<String, Object> simpleMap = new HashMap<>();

            for (int i = 0; i < args.length; i++) {
                if (args[i] == null || shouldFilterParam(args[i])) continue;

                String key = "param" + i;
                String value;

                if (args[i] instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) args[i];
                    value = "File: " + file.getOriginalFilename();
                } else if (args[i] instanceof Collection) {
                    value = "Collection[size=" + ((Collection<?>) args[i]).size() + "]";
                } else if (args[i] instanceof Map) {
                    value = "Map[size=" + ((Map<?, ?>) args[i]).size() + "]";
                } else {
                    value = args[i].getClass().getSimpleName();
                }

                simpleMap.put(key, value);
            }

            return objectMapper.writeValueAsString(simpleMap);
        } catch (Exception e) {
            log.error("获取简化参数失败", e);
            return null;
        }
    }

    /**
     * 从请求中提取参数（用于拦截器方式）
     */
    public String extractParamsFromRequest(HttpServletRequest request) {
        try {
            ObjectNode paramsNode = objectMapper.createObjectNode();

            // 获取Query参数
            Map<String, String[]> queryParams = request.getParameterMap();
            if (!queryParams.isEmpty()) {
                ObjectNode queryNode = objectMapper.createObjectNode();
                for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();

                    if (values.length == 1) {
                        queryNode.put(key, values[0]);
                    } else {
                        ArrayNode arrayNode = objectMapper.createArrayNode();
                        for (String value : values) {
                            arrayNode.add(value);
                        }
                        queryNode.set(key, arrayNode);
                    }
                }
                paramsNode.set("query", queryNode);
            }

            return paramsNode.size() > 0 ? objectMapper.writeValueAsString(paramsNode) : null;
        } catch (Exception e) {
            log.warn("从请求提取参数失败", e);
            return null;
        }
    }

    /**
     * 判断是否为敏感Header
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("auth") ||
                lowerName.contains("token") ||
                lowerName.contains("password") ||
                lowerName.contains("secret") ||
                lowerName.contains("key") ||
                "cookie".equals(lowerName);
    }
}