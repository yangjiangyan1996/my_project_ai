package com.example.aop.task;

import org.springframework.context.ApplicationEvent;

public class TaskCompletedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long taskId;
    private final Integer rewardType;
    private final String rewardValue;

    // 使用Object作为事件源
    public TaskCompletedEvent(Object source, Long userId, Long taskId,
                              Integer rewardType, String rewardValue) {
        super(source);  // 必须调用父类构造函数
        this.userId = userId;
        this.taskId = taskId;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
    }

    // Getter方法
    public Long getUserId() {
        return userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Integer getRewardType() {
        return rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }
}