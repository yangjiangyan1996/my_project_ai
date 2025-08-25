package com.example.job;

import com.example.service.AutoProjectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "auto-publish.enabled", havingValue = "true")
public class AutoPublishScheduleConfig {

    @Resource
    AutoProjectService autoProjectService;

    @Value("${auto-publish.schedule}")
    private String cronExpression;

    @Scheduled(cron = "${auto-publish.schedule}")
    public void scheduledAutoPublish() {
        log.info("开始执行自动发布项目任务...");
        Long projectId = autoProjectService.autoPublishProject();
        if (projectId != null) {
            log.info("自动发布项目成功，项目ID: {}", projectId);
        } else {
            log.info("未发布新项目(可能已达每日上限或没有活跃用户)");
        }
    }
}