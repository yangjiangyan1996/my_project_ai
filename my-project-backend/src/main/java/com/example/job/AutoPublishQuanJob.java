package com.example.job;

import com.example.service.AutoProjectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "auto-publish.enabled", havingValue = "true")
public class AutoPublishQuanJob {

    @Resource
    AutoProjectService autoProjectService;

    @Scheduled(cron = "${auto-publish.schedule}")
    public void scheduledAutoPublish() {
        log.info("开始执行自动发布帖子任务...");
        autoProjectService.autoQuanTieProject();
        log.info("开始执行自动发布帖子任务成功");
    }
}