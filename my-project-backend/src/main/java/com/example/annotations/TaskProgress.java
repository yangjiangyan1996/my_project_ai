package com.example.annotations;

import com.example.enums.AchievementEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskProgress {
    /**
     * 任务分类标识（对应task_definition表中的category字段）
     * @return
     */
    AchievementEnums.Category[] category();
    
    /**
     * 进度增加值（默认加1）
     */
    int increment() default 1;
    
    /**
     * 是否必须成功才记录进度（默认true）
     */
    boolean onlyOnSuccess() default true;
}