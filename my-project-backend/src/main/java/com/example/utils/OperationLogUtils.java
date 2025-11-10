package com.example.utils;

import com.example.service.CkOperationLogService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationLogUtils {

    @Resource
    private CkOperationLogService operationLogService;

    /**
     * 快速记录操作日志
     */
    public void log(String module, String operation, String description) {
        operationLogService.saveLog(module, operation, description, null);
    }

    public void log(String module, String operation, String description, Long targetId) {
        operationLogService.saveLog(module, operation, description, targetId);
    }

    /**
     * 记录创建操作
     */
    public void logCreate(String module, String description, Long targetId) {
        operationLogService.saveLog(module, "CREATE", description, targetId);
    }

    /**
     * 记录更新操作
     */
    public void logUpdate(String module, String description, Long targetId) {
        operationLogService.saveLog(module, "UPDATE", description, targetId);
    }

    /**
     * 记录删除操作
     */
    public void logDelete(String module, String description, Long targetId) {
        operationLogService.saveLog(module, "DELETE", description, targetId);
    }

    /**
     * 记录查询操作
     */
    public void logQuery(String module, String description) {
        operationLogService.saveLog(module, "QUERY", description, null);
    }
}