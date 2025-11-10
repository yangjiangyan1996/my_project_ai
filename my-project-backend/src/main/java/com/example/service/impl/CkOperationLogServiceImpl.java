package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.OperationLog;
import com.example.mapper.CkOperationLogMapper;
import com.example.service.CkOperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CkOperationLogServiceImpl extends ServiceImpl<CkOperationLogMapper, OperationLog>
    implements CkOperationLogService {

    @Resource
    private  CkOperationLogMapper operationLogMapper;

    @Override
    public void saveOperationLog(OperationLog log) {
        // 设置默认值
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }
        if (log.getModifiedAt() == null) {
            log.setModifiedAt(new Date());
        }
        if (log.getIsDeleted() == null) {
            log.setIsDeleted(0);
        }
        
        operationLogMapper.insert(log);
    }

    @Override
    public void saveLog(String module, String operation, String description, Long targetId) {
        // 从当前线程获取用户信息（需要根据你的认证系统调整）
        Long currentUserId = getCurrentUserId();
        Long currentTenantId = getCurrentTenantId();
        
        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : "";
        
        saveLog(currentTenantId, currentUserId, module, operation, description, 
                targetId, ipAddress, userAgent);
    }

    @Override
    public void saveLog(Long tenantId, Long userId, String module, String operation, 
                       String description, Long targetId, String ipAddress, String userAgent) {
        
        OperationLog log = new OperationLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setModule(module);
        log.setOperation(operation);
        log.setDescription(description);
        log.setTargetId(targetId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCreatedBy(userId);
        log.setModifiedBy(userId);
        log.setCreatedAt(new Date());
        log.setModifiedAt(new Date());
        log.setIsDeleted(0);
        
        saveOperationLog(log);
    }
    
    /**
     * 获取当前用户ID（需要根据你的认证系统实现）
     */
    private Long getCurrentUserId() {
        // 示例：从SecurityContext或Session中获取
        // 这里需要根据你的用户认证系统进行调整
        try {
            // 假设你使用Spring Security
            // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // if (authentication != null && authentication.isAuthenticated()) {
            //     YourUserDetails userDetails = (YourUserDetails) authentication.getPrincipal();
            //     return userDetails.getId();
            // }
            return 1L; // 临时返回默认值，请根据实际情况修改
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * 获取当前租户ID（需要根据你的多租户系统实现）
     */
    private Long getCurrentTenantId() {
        // 示例：从ThreadLocal或Session中获取租户信息
        // 这里需要根据你的多租户系统进行调整
        try {
            return 1L; // 临时返回默认值，请根据实际情况修改
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        
        return ip;
    }
}