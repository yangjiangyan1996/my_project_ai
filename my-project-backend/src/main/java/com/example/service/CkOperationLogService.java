package com.example.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.cangku.dto.OperationLog;

public interface CkOperationLogService extends IService<OperationLog> {
    
    /**
     * 保存操作日志
     */
    void saveOperationLog(OperationLog log);
    
    /**
     * 快速保存操作日志（简化参数）
     */
    void saveLog(String module, String operation, String description, Long targetId);
    
    /**
     * 保存操作日志（完整参数）
     */
    void saveLog(Long tenantId, Long userId, String module, String operation, 
                String description, Long targetId, String ipAddress, String userAgent);
}