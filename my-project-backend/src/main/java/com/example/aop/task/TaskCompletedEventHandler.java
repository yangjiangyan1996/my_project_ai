package com.example.aop.task;

import com.example.Facade.TaskFacade;
import com.example.entity.vo.TaskCompletedEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCompletedEventHandler {
    @Resource
    private TaskFacade taskFacade;

    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        // 根据奖励类型处理不同的奖励
        switch (event.getRewardType()) {
            case 1: // 积分
                taskFacade.addPoints(event.getUserId(), event.getRewardValue());
                break;
            case 2: // 勋章
                taskFacade.awardBadge(event.getUserId(), event.getTaskId());
                break;
            case 3: // 经验值
                taskFacade.addExperience(event.getUserId(), event.getRewardValue());
                break;
        }
        
        // 可以发送通知等后续处理
    }
}