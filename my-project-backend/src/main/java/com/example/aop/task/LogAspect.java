package com.example.aop.task;

import com.example.annotations.LogOperation;
import com.example.entity.cangku.dto.OperationLog;
import com.example.filter.UserUtil;
import com.example.mapper.CkOperationLogMapper;
import com.example.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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
    private RequestParamUtil requestParamUtil;

    @Resource
    private CkOperationLogMapper operationLogMapper;

    // 定义切点：标记了@LogOperation注解的方法
    @Pointcut("@annotation(com.example.annotations.LogOperation)")
    public void logPointCut() {
    }

    /**
     * 使用 @Around 环绕通知来记录方法执行时间
     */
    @Around("@annotation(logAnnotation)")
    public Object aroundLog(ProceedingJoinPoint joinPoint, LogOperation logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // 根据是否有异常调用不同的记录方法
            if (exception != null) {
                saveExceptionLog(joinPoint, logAnnotation, exception, responseTime);
            } else {
                saveLog(joinPoint, logAnnotation, result, responseTime);
            }
        }
    }

    /**
     * 正常执行的日志记录
     */
    private void saveLog(JoinPoint joinPoint, LogOperation logAnnotation, Object result, long responseTime) {
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
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setRequestMethod(request.getMethod());
            }

            // 设置响应时间
            operationLog.setResponseTime(responseTime);

            // 提取目标ID和请求参数
            extractTargetIdAndParams(operationLog, method, args);

            // 记录返回结果
            recordReturnResult(operationLog, result);

            // 设置成功状态
            operationLog.setStatus(1); // 1-成功
            operationLog.setStatusMessage("操作成功");

            // 保存日志
            operationLogMapper.insert(operationLog);
            log.debug("操作日志记录成功：{}，耗时：{}ms", operationLog.getDescription(), responseTime);

        } catch (Exception e) {
            log.error("记录操作日志异常", e);
        }
    }

    /**
     * 异常情况的日志记录
     */
    private void saveExceptionLog(JoinPoint joinPoint, LogOperation logAnnotation, Exception ex, long responseTime) {
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
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setRequestMethod(request.getMethod());
            }

            // 设置响应时间
            operationLog.setResponseTime(responseTime);

            // 提取目标ID和请求参数
            extractTargetIdAndParams(operationLog, method, args);

            // 记录异常信息
            operationLog.setStatus(0); // 0-失败
            operationLog.setStatusMessage(ex.getMessage());

            // 记录异常堆栈（限制长度）
            String exceptionStackTrace = getExceptionStackTrace(ex);
            if (exceptionStackTrace.length() > 1000) {
                exceptionStackTrace = exceptionStackTrace.substring(0, 1000) + "...";
            }
            operationLog.setResponseData(exceptionStackTrace);

            // 保存日志
            operationLogMapper.insert(operationLog);
            log.debug("异常操作日志记录成功：{}，耗时：{}ms", operationLog.getDescription(), responseTime);

        } catch (Exception e) {
            log.error("记录异常操作日志异常", e);
        }
    }

    /**
     * 记录返回结果
     */
    private void recordReturnResult(OperationLog operationLog, Object result) {
        if (result == null) {
            operationLog.setResponseData("null");
            return;
        }

        try {
            String responseJson;
            if (result instanceof String) {
                responseJson = (String) result;
            } else {
                responseJson = requestParamUtil.getObjectMapper().writeValueAsString(result);
            }

            // 限制响应数据长度，避免过大
            if (responseJson.length() > 2000) {
                responseJson = responseJson.substring(0, 2000) + "...";
            }

            operationLog.setResponseData(responseJson);

            // 尝试提取成功状态
            extractSuccessStatus(operationLog, result);

        } catch (Exception e) {
            // 如果序列化失败，使用toString
            String resultStr = result.toString();
            if (resultStr.length() > 1000) {
                resultStr = resultStr.substring(0, 1000) + "...";
            }
            operationLog.setResponseData(resultStr);
            log.warn("序列化返回结果失败，使用toString", e);
        }
    }

    /**
     * 从返回结果中提取成功状态
     */
    private void extractSuccessStatus(OperationLog operationLog, Object result) {
        try {
            if (result == null) return;

            // 如果返回的是标准的REST响应格式
            String resultStr = result.toString();

            // 检查是否包含错误信息
            if (resultStr.contains("\"success\":false") ||
                    resultStr.contains("\"code\":\"ERROR\"") ||
                    resultStr.contains("\"msg\":\"失败\"")) {
                operationLog.setStatus(0); // 失败
                operationLog.setStatusMessage("业务逻辑失败");
            } else if (resultStr.contains("\"success\":true") ||
                    resultStr.contains("\"code\":\"SUCCESS\"") ||
                    resultStr.contains("\"msg\":\"成功\"")) {
                operationLog.setStatus(1); // 成功
                operationLog.setStatusMessage("操作成功");
            }

        } catch (Exception e) {
            // 忽略提取失败
        }
    }

    /**
     * 获取异常堆栈
     */
    private String getExceptionStackTrace(Exception ex) {
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");

        for (StackTraceElement element : ex.getStackTrace()) {
            stackTrace.append("    at ").append(element.toString()).append("\n");
        }

        return stackTrace.toString();
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
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        // 策略3：从对象参数中提取id字段
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
                    // 继续尝试其他字段
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
        return IpUtils.getIpAddress(request);
    }
}