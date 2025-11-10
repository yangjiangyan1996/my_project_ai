package com.example.aop.task;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录参数
     */
    boolean logParams() default true;
    
    /**
     * 是否记录返回值
     */
    boolean logResult() default false;
    
    /**
     * 目标ID参数名（从方法参数中获取）
     */
    String targetId() default "";
}