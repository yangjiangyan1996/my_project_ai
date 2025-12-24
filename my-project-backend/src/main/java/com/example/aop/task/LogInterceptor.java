//package com.example.aop.task;
//
//import com.example.annotations.LogOperation;
//import com.example.entity.base.UserInfo;
//import com.example.entity.cangku.dto.OperationLog;
//import com.example.filter.UserUtil;
//import com.example.mapper.CkOperationLogMapper;
//import com.example.utils.IpUtils;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.lang.reflect.Method;
//import java.util.Date;
//
//@Component
//@Slf4j
//public class LogInterceptor implements HandlerInterceptor {
//
//    @Resource
//    private RequestParamUtil requestParamUtil;
//
//    @Resource
//    private CkOperationLogMapper operationLogMapper;
//
//    // 使用 ThreadLocal 存储请求开始时间
//    private static final ThreadLocal<Long> REQUEST_START_TIME = new ThreadLocal<>();
//
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response,
//                             Object handler) throws Exception {
//        // 只处理 HandlerMethod 类型的处理器（即 Controller 方法）
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod();
//
//            // 检查方法是否有 @LogOperation 注解
//            LogOperation logAnnotation = method.getAnnotation(LogOperation.class);
//            if (logAnnotation != null) {
//                log.debug("检测到 @LogOperation 注解，开始记录请求时间: {} {}",
//                        request.getMethod(), request.getRequestURI());
//                REQUEST_START_TIME.set(System.currentTimeMillis());
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request,
//                                HttpServletResponse response,
//                                Object handler,
//                                Exception ex) {
//        try {
//            // 只处理 HandlerMethod 类型的处理器
//            if (!(handler instanceof HandlerMethod)) {
//                return;
//            }
//
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod();
//
//            // 检查方法是否有 @LogOperation 注解
//            LogOperation logAnnotation = method.getAnnotation(LogOperation.class);
//            if (logAnnotation == null) {
//                log.debug("方法没有 @LogOperation 注解，跳过日志记录: {}", method.getName());
//                return;
//            }
//
//            Long startTime = REQUEST_START_TIME.get();
//            if (startTime == null) {
//                log.debug("请求开始时间为空，跳过日志记录");
//                return;
//            }
//
//            // 计算耗时
//            long duration = System.currentTimeMillis() - startTime;
//
//            String uri = request.getRequestURI();
//            String httpMethod = request.getMethod();
//            int status = response.getStatus();
//
//            // 获取用户信息
//            UserInfo user = getUserInfo();
//            if (user == null) {
//                log.debug("用户信息为空，跳过日志记录");
//                return;
//            }
//
//            log.debug("开始记录操作日志: {} {}", httpMethod, uri);
//
//            OperationLog operationLog = new OperationLog();
//
//            // 从注解获取信息
//            operationLog.setModule(logAnnotation.module());
//            operationLog.setOperation(logAnnotation.operation());
//            operationLog.setDescription(logAnnotation.description());
//
//            // 设置用户和租户信息
//            operationLog.setTenantId(user.getTenantId());
//            operationLog.setUserId(user.getId());
//            operationLog.setCreatedBy(user.getId());
//            operationLog.setCreatedAt(new Date());
//
//            // 设置请求信息
//            operationLog.setIpAddress(getClientIp(request));
//            operationLog.setUserAgent(request.getHeader("User-Agent"));
//            operationLog.setRequestUrl(uri);
//            operationLog.setRequestMethod(httpMethod);
//            operationLog.setResponseTime(duration);
//            operationLog.setResponseStatus(status);
//
//            // 根据HTTP状态码设置操作状态
//            if (status >= 200 && status < 300) {
//                operationLog.setStatus(1); // 成功
//                operationLog.setStatusMessage("HTTP " + status);
//            } else if (status >= 400 && status < 500) {
//                operationLog.setStatus(2); // 客户端错误
//                operationLog.setStatusMessage("客户端错误 HTTP " + status);
//            } else if (status >= 500) {
//                operationLog.setStatus(0); // 服务器错误
//                operationLog.setStatusMessage("服务器错误 HTTP " + status);
//            } else {
//                operationLog.setStatus(3); // 其他状态
//                operationLog.setStatusMessage("HTTP " + status);
//            }
//
//            // 如果有异常，记录异常信息
//            if (ex != null) {
//                operationLog.setStatus(0); // 失败
//                operationLog.setStatusMessage(ex.getMessage());
//                // 记录异常堆栈（限制长度）
//                String exceptionStackTrace = getExceptionStackTrace(ex);
//                if (exceptionStackTrace.length() > 1000) {
//                    exceptionStackTrace = exceptionStackTrace.substring(0, 1000) + "...";
//                }
//                operationLog.setResponseData(exceptionStackTrace);
//            } else {
//                // 如果没有异常，记录正常的响应数据
//                // 注意：这里只能记录HTTP状态码，无法获取Controller返回的数据
//                operationLog.setResponseData("HTTP Status: " + status);
//            }
//
//            // 提取请求参数
//            try {
//                // 从请求中提取参数（GET/POST表单参数）
//                String requestParams = requestParamUtil.extractParamsFromRequest(request);
//                if (requestParams != null) {
//                    operationLog.setRequestParams(requestParams);
//                }
//
//                // 注意：对于 @RequestBody 参数，这里无法获取到
//                // 如果需要记录 @RequestBody 参数，建议使用 AOP 方式
//            } catch (Exception e) {
//                log.warn("提取请求参数失败: {}", e.getMessage());
//            }
//
//            // 尝试从URL路径中提取目标ID
//            try {
//                Long targetId = extractTargetIdFromUri(uri);
//                if (targetId != null) {
//                    operationLog.setTargetId(targetId);
//                }
//            } catch (Exception e) {
//                log.warn("提取目标ID失败: {}", e.getMessage());
//            }
//
//            // 保存日志
//            try {
//                //operationLogMapper.insert(operationLog);
//                log.debug("操作日志记录成功: {}", operationLog.getDescription());
//            } catch (Exception e) {
//                log.error("保存操作日志失败", e);
//            }
//
//        } catch (Exception e) {
//            log.error("日志记录过程中发生异常", e);
//        } finally {
//            // 清理ThreadLocal
//            REQUEST_START_TIME.remove();
//        }
//    }
//
//    /**
//     * 获取用户信息（处理可能的异常）
//     */
//    private UserInfo getUserInfo() {
//        try {
//            return UserUtil.getCurrentUser();
//        } catch (Exception e) {
//            log.warn("获取用户信息失败: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * 获取异常堆栈
//     */
//    private String getExceptionStackTrace(Exception ex) {
//        StringBuilder stackTrace = new StringBuilder();
//        stackTrace.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
//
//        for (StackTraceElement element : ex.getStackTrace()) {
//            stackTrace.append("    at ").append(element.toString()).append("\n");
//        }
//
//        return stackTrace.toString();
//    }
//
//    /**
//     * 从URL路径中提取目标ID
//     */
//    private Long extractTargetIdFromUri(String uri) {
//        try {
//            // 匹配类似 /product/123 的路径
//            String[] segments = uri.split("/");
//            for (int i = segments.length - 1; i >= 0; i--) {
//                if (segments[i].matches("\\d+")) {
//                    return Long.parseLong(segments[i]);
//                }
//            }
//        } catch (Exception e) {
//            // 忽略解析错误
//        }
//        return null;
//    }
//
//    private String getClientIp(HttpServletRequest request) {
//        return IpUtils.getIpAddress(request);
//    }
//}