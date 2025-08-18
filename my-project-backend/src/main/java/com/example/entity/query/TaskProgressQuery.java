package com.example.entity.query;

import com.example.entity.dto.TaskDefinition;
import com.example.enums.AchievementEnums;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 16:57
 */
@Data
public class TaskProgressQuery {
    private Long userId;
    private Long taskId;
    private Date startTime;
    private Date endTime;

    public TaskProgressQuery(TaskDefinition task) {
        if (task.getType().equals(AchievementEnums.TaskType.ONETIME.getCode())) {//一次性
            //一次性的时间不处理
        } else if (task.getType().equals(AchievementEnums.TaskType.DAILY.getCode())) {//日常
            //startTime为今日的00:00:00
            startTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            endTime = new Date(System.currentTimeMillis());
        } else if (task.getType().equals(AchievementEnums.TaskType.WEEKLY.getCode())) {//每周
            //startTime为本周的周一的00:00:00
            startTime = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
            endTime = new Date(System.currentTimeMillis());
        } else {
            throw new RuntimeException("任务类型不存在 " + task.getType());
        }
    }
}
