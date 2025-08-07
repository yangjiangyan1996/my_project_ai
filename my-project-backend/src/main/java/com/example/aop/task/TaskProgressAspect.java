package com.example.aop.task;

import com.example.Facade.TaskFacade;
import com.example.annotations.TaskProgress;
import com.example.config.AsyncTaskUtil;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TaskProgressAspect {
    @Resource
    TaskFacade taskFacade;
    
    // 拦截所有带有@TaskProgress注解的方法
    @Around("@annotation(taskProgress)")
    public Object updateTaskProgress(ProceedingJoinPoint joinPoint, TaskProgress taskProgress) throws Throwable {
        // 1. 执行原方法
        Object result = joinPoint.proceed();
        
        // 2. 如果配置了onlyOnSuccess=true且方法抛出异常，则不记录进度
        if (taskProgress.onlyOnSuccess()
                && result instanceof RespBean
                && ((RespBean<?>) result).getCode() != 200) {
            return result;
        }
        
        // 3. 获取当前用户ID（根据你的认证系统调整）
        Long userId = getCurrentUserId();
        if (userId == null) {
            return result;
        }
        
        // 4. 异步更新任务进度
        AsyncTaskUtil.execute(() -> {
            taskFacade.updateProgressAsync(
                    userId,
                    taskProgress.category(),
                    taskProgress.increment()
            );
        });
        return result;
    }
    
    private Long getCurrentUserId() {
        // 实现获取当前用户ID的逻辑
        // 例如从SecurityContext中获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            return ((UserInfo) authentication.getPrincipal()).getId();
        }
        return null;
    }
}