package com.example.job;

//@Slf4j
//@Configuration
//@EnableScheduling
//@ConditionalOnProperty(name = "auto-publish.enabled", havingValue = "true")
//public class AutoPublishQuanJob {
//
//    @Resource
//    AutoProjectService autoProjectService;
//
//    @Scheduled(cron = "${auto-publish.schedule}")
//    public void scheduledAutoPublish() {
//        log.info("开始执行自动发布帖子任务...");
//        autoProjectService.autoQuanTieProject();
//        log.info("开始执行自动发布帖子任务成功");
//    }
//}