package com.example.aop.task;

import com.alibaba.fastjson2.JSON;
import com.example.entity.dto.TaskDefinition;
import com.example.entity.dto.TaskUserProgress;
import com.example.entity.query.TaskProgressQuery;
import com.example.entity.vo.TaskCompletedEvent;
import com.example.enums.AchievementEnums;
import com.example.service.TaskDefinitionService;
import com.example.service.TaskUserProgressService;
import io.lettuce.core.internal.LettuceLists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskProgressService {
    private final TaskUserProgressService taskUserProgressService;
    private final TaskDefinitionService taskDefinitionService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void updateProgressAsync(Long userId, AchievementEnums.Category[] category, int increment) {
        // 1. 查询该分类下的所有活跃任务
        List<String> categoryCodeList = Arrays.stream(category).map(v -> v.getCode()).collect(Collectors.toList());
        List<TaskDefinition> tasks = taskDefinitionService.findByCategoryAndStatus(categoryCodeList, 1);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        // 2. 对每个任务更新进度
        tasks.forEach(task -> {
            TaskProgressQuery query = new TaskProgressQuery(task);
            query.setUserId(userId);
            query.setTaskId(task.getId());

            // 获取或创建用户任务进度记录
            TaskUserProgress progress = taskUserProgressService
                    .findByUserIdAndTaskId(query)
                    .orElseGet(() -> createNewProgress(userId, task));

            // 如果任务已完成，则跳过
            if (progress.getStatus().equals(AchievementEnums.ProgressStatus.COMPLETED.getCode())) {
                return;
            }

            // 更新进度
            Integer newValue = progress.getCurrentValue() + increment;
            progress.setCurrentValue(newValue);

            // 检查是否完成任务
            if (newValue >= task.getTargetValue()) {
                progress.setStatus(AchievementEnums.ProgressStatus.COMPLETED.getCode()); // 2=已完成
                progress.setCompletedTime(new Date());

                // 发布任务完成事件
                eventPublisher.publishEvent(new TaskCompletedEvent(this,
                        userId,
                        task.getId(),
                        task.getRewardType(),
                        task.getRewardValue()
                ));
            } else {
                progress.setStatus(AchievementEnums.ProgressStatus.IN_PROGRESS.getCode()); // 1=进行中
            }

            progress.setLastProgressTime(new Date());

            boolean saveOrUpdate = false;
            if (progress.getId() == null) {
                saveOrUpdate = taskUserProgressService.save(progress);
            } else {
                saveOrUpdate = taskUserProgressService.updateById(progress);
            }
            if (!saveOrUpdate) {
                log.error("TaskProgressService#updateProgressAsync,更新任务进度失败:{}", JSON.toJSONString(progress));
            }
        });
    }

    private TaskUserProgress createNewProgress(Long userId, TaskDefinition task) {
        TaskUserProgress progress = new TaskUserProgress();
        progress.setUserId(userId);
        progress.setTaskId(task.getId());
        progress.setCurrentValue(0);
        progress.setStatus(AchievementEnums.ProgressStatus.NOT_STARTED.getCode());
        progress.setRewardClaimed(AchievementEnums.RewardStatus.NOT_CLAIMED.getCode());
        progress.setLastProgressTime(new Date());
        return progress;
    }
}