package com.example.aop.task;

import com.example.entity.cangku.dto.OperationLog;
import com.example.filter.UserUtil;
import com.example.annotations.LogOperation;
import com.example.mapper.CkOperationLogMapper;
import com.example.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
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

    // 定义切点：Controller包下的所有方法
    @Pointcut("execution(* com.example.controller.cangku..*.*(..))")
    public void controllerPointCut() {
    }

    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void saveLog(JoinPoint joinPoint, LogOperation logAnnotation, Object result) {
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

            // 提取目标ID和请求参数
            extractTargetIdAndParams(operationLog, method, args);

            // 记录返回结果
            recordReturnResult(operationLog, result);

            // 设置成功状态
            operationLog.setStatus(1); // 1-成功
            operationLog.setStatusMessage("操作成功");

            // 保存日志
            operationLogMapper.insert(operationLog);
            log.debug("操作日志记录成功：{}", operationLog.getDescription());

        } catch (Exception e) {
            log.error("记录操作日志异常", e);
        }
    }

    /**
     * 异常情况的日志记录
     */
    @AfterThrowing(pointcut = "@annotation(logAnnotation)", throwing = "ex")
    public void saveExceptionLog(JoinPoint joinPoint, LogOperation logAnnotation, Exception ex) {
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
            log.debug("异常操作日志记录成功：{}", operationLog.getDescription());

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