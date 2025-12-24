package com.example.aop.task;

import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.OperationLog;
import com.example.filter.UserUtil;
import com.example.mapper.CkOperationLogMapper;
import com.example.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Resource
    private RequestParamUtil requestParamUtil;
    @Resource
    private CkOperationLogMapper operationLogMapper;
    
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();

            UserInfo user = UserUtil.getCurrentUser();
            // 只记录特定请求，避免日志过多
            if (shouldLog(uri, method)) {
                OperationLog log = new OperationLog();
                log.setTenantId(user.getTenantId());
                log.setUserId(user.getId());
                log.setModule(getModuleFromUri(uri));
                log.setOperation(method);
                log.setDescription(String.format("%s %s", method, uri));
                log.setIpAddress(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setCreatedAt(new Date());
                log.setCreatedBy(user.getId());

                // 提取请求参数
                String requestParams = requestParamUtil.extractParamsFromRequest(request);
                if (requestParams != null) {
                    log.setRequestParams(requestParams);
                }

                // 尝试从URL路径中提取目标ID
                Long targetId = extractTargetIdFromUri(uri);
                if (targetId != null) {
                    log.setTargetId(targetId);
                }
                
                operationLogMapper.insert(log);
            }
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            log.error("日志记录失败", e);
        }
    }

    /**
     * 从URL路径中提取目标ID
     */
    private Long extractTargetIdFromUri(String uri) {
        try {
            // 匹配类似 /product/123 的路径
            String[] segments = uri.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (segments[i].matches("\\d+")) {
                    return Long.parseLong(segments[i]);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }


    private boolean shouldLog(String uri, String method) {
        // 排除健康检查、静态资源等
        return !uri.contains("/health") 
            && !uri.contains("/swagger") 
            && !uri.contains("/api-docs")
            && !uri.contains(".js")
            && !uri.contains(".css")
            && !uri.contains(".ico");
    }
    
    private String getModuleFromUri(String uri) {
        if (uri.contains("/product")) return "产品管理";
        if (uri.contains("/inbound")) return "入库管理";
        if (uri.contains("/outbound")) return "出库管理";
        if (uri.contains("/customer")) return "客户管理";
        if (uri.contains("/warehouse")) return "仓库管理";
        if (uri.contains("/supplier")) return "供应商管理";
        if (uri.contains("/unit")) return "单位管理";
        if (uri.contains("/recommend")) return "推荐规则管理";
        if (uri.contains("/shelf")) return "货架管理";
        if (uri.contains("/sku")) return "sku管理";
        if (uri.contains("/user")) return "用户管理";
        if (uri.contains("/tenant")) return "机构管理";
        return "系统管理";
    }
    
    private String getClientIp(HttpServletRequest request) {
        return IpUtils.getIpAddress( request);
    }
}