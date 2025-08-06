package com.example.aop.task;

import com.example.entity.dto.TaskDefinition;
import com.example.entity.dto.TaskUserProgress;
import com.example.service.TaskDefinitionService;
import com.example.service.TaskUserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskProgressService {
    private final TaskUserProgressService taskUserProgressService;
    private final TaskDefinitionService taskDefinitionService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Async
    public void updateProgressAsync(Long userId, String category, int increment) {
        // 1. 查询该分类下的所有活跃任务
        List<TaskDefinition> tasks = taskDefinitionService.findByCategoryAndStatus(category, 1);
        if (CollectionUtils.isEmpty( tasks)) {
            return;
        }
        
        // 2. 对每个任务更新进度
        tasks.forEach(task -> {
            // 获取或创建用户任务进度记录
            TaskUserProgress progress = taskUserProgressService
                .findByUserIdAndTaskId(userId, task.getId())
                .orElseGet(() -> createNewProgress(userId, task));
            
            // 如果任务已完成且不可重复，则跳过
            if (progress.getStatus() == 2 && task.getType() == 3) { // 3=一次性任务
                return;
            }
            
            // 更新进度
            Integer newValue = progress.getCurrentValue() + increment;
            progress.setCurrentValue(newValue);
            
            // 检查是否完成任务
            if (newValue >= task.getTargetValue()) {
                progress.setStatus(2); // 2=已完成
                progress.setCompletedTime(new Date());
                
                // 发布任务完成事件
                eventPublisher.publishEvent(new TaskCompletedEvent(this,
                    userId, 
                    task.getId(),
                    task.getRewardType(),
                    task.getRewardValue()
                ));
            } else {
                progress.setStatus(1); // 1=进行中
            }
            
            progress.setLastProgressTime(new Date());
            taskUserProgressService.save(progress);
        });
    }
    
    private TaskUserProgress createNewProgress(Long userId, TaskDefinition task) {
        TaskUserProgress progress = new TaskUserProgress();
        progress.setUserId(userId);
        progress.setTaskId(task.getId());
        progress.setCurrentValue(0);
        progress.setStatus(0); // 0=未开始
        progress.setRewardClaimed(0);
        return progress;
    }
}