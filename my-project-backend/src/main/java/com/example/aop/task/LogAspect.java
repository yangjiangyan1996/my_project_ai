package com.example.aop.task;

import com.example.entity.cangku.dto.OperationLog;
import com.example.filter.UserUtil;
import com.example.annotations.LogOperation;
import com.example.mapper.CkOperationLogMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    @Resource
    RequestParamUtil requestParamUtil;
    @Resource
    private CkOperationLogMapper operationLogMapper;
    
    // 定义切点：标记了@LogOperation注解的方法
    @Pointcut("@annotation(com.example.annotations.LogOperation)")
    public void logPointCut() {
    }
    
    // 定义切点：Controller包下的所有方法（可选，按需使用）
    @Pointcut("execution(* com.example.controller.cangku..*.*(..))")
    public void controllerPointCut() {
    }

    @AfterReturning(pointcut = "@annotation(logAnnotation)")
    public void saveLog(JoinPoint joinPoint, LogOperation logAnnotation) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();

            OperationLog operationLog = new OperationLog();

            // 设置基本信息
            operationLog.setModule(logAnnotation.module());
            operationLog.setOperation(logAnnotation.operation());
            operationLog.setDescription(logAnnotation.description());

            // 设置用户和租户信息
            operationLog.setTenantId(UserUtil.getCurrentUser().getTenantId());
            operationLog.setUserId(UserUtil.getCurrentUserId());
            operationLog.setCreatedBy(UserUtil.getCurrentUserId());

            // 设置请求信息
            HttpServletRequest request = getRequest();
            if (request != null) {
                operationLog.setIpAddress(getIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // 提取目标ID和请求参数
            extractTargetIdAndParams(operationLog, method, args);

            // 保存日志
            operationLogMapper.insert(operationLog);
            log.debug("操作日志记录成功：{}，参数：{}",
                    operationLog.getDescription(),
                    operationLog.getRequestParams());

        } catch (Exception e) {
            log.error("记录操作日志异常", e);
        }
    }

    /**
     * 提取目标ID和请求参数
     */
    private void extractTargetIdAndParams(OperationLog operationLog,
                                          Method method,
                                          Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        try {
            // 1. 尝试提取目标ID
            Long targetId = extractTargetId(method, args);
            if (targetId != null) {
                operationLog.setTargetId(targetId);
            }

            // 2. 转换请求参数为JSON
            String requestParams = requestParamUtil.convertParamsToJson(method, args);
            if (requestParams != null && !requestParams.isEmpty()) {
                operationLog.setRequestParams(requestParams);
            }

        } catch (Exception e) {
            log.warn("提取目标ID或请求参数失败", e);
        }
    }

    /**
     * 提取目标ID的多种策略
     */
    private Long extractTargetId(Method method, Object[] args) {
        // 策略1：从方法参数中提取名为"id"的参数
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Long) {
                String paramName = getParameterName(method, i);
                if ("id".equals(paramName) || paramName.endsWith("Id")) {
                    return (Long) args[i];
                }
            }
        }

        // 策略2：从路径变量中提取（@PathVariable）
        // 这里需要结合注解解析，简化处理：查找第一个Long类型的参数
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        // 策略3：从对象参数中提取id字段（如ProductDTO）
        for (Object arg : args) {
            if (arg != null && !arg.getClass().isPrimitive() && !isSimpleType(arg)) {
                try {
                    java.lang.reflect.Field idField = arg.getClass().getDeclaredField("id");
                    if (idField != null) {
                        idField.setAccessible(true);
                        Object idValue = idField.get(arg);
                        if (idValue instanceof Long) {
                            return (Long) idValue;
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // 忽略，继续尝试其他字段
                }
            }
        }

        return null;
    }

    /**
     * 获取参数名
     */
    private String getParameterName(Method method, int index) {
        if (index < method.getParameters().length) {
            return method.getParameters()[index].getName();
        }
        return "arg" + index;
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
                obj.getClass().isPrimitive();
    }

    // 原有方法保持不变...
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        // 实现同上，省略...
        return "";
    }
}