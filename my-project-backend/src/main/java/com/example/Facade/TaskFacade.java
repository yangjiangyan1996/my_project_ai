package com.example.Facade;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.constants.CommonConstant;
import com.example.entity.dto.*;
import com.example.entity.query.TaskProgressQuery;
import com.example.entity.req.AchievementMyPageReq;
import com.example.entity.resp.AchievementBaseInfoResp;
import com.example.entity.resp.AchievementMyPageResp;
import com.example.entity.resp.TaskBadgeResp;
import com.example.entity.vo.TaskCompletedEvent;
import com.example.enums.AchievementEnums;
import com.example.service.*;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/6 10:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskFacade {

    @Resource
    TaskUserProgressService taskUserProgressService;
    @Resource
    TaskDefinitionService taskDefinitionService;
    @Resource
    TaskUserBadgeService taskUserBadgeService;
    @Resource
    TaskBadgeService taskBadgeService;
    @Resource
    TaskUserPointService taskUserPointService;
    @Resource
    ApplicationEventPublisher eventPublisher;

    @Async
    public void updateProgressAsync(Long userId, String[] category, int increment) {
        // 1. 查询该分类下的所有活跃任务
        List<String> categoryCodeList = Arrays.stream(category).collect(Collectors.toList());
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

                // 发布任务完成事件  TODO yang  搜【后续在看需要完成什么内容】
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

    public Page<AchievementMyPageResp> myAchievementPageList(AchievementMyPageReq req) {
        Page<TaskDefinition> list = taskDefinitionService.myAchievementPageList(Page.of(req.getPage(), req.getSize()), req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<TaskUserProgress> myTaskList = taskUserProgressService.selectByUserId(req.getUserId(), null);
        Map<Long, TaskUserProgress> taskId2TaskInfoMap = myTaskList.stream().collect(Collectors.toMap(v -> v.getTaskId(), v -> v));

        List<AchievementMyPageResp> collect = list.getRecords().stream().map(v -> {
                    AchievementMyPageResp p = new AchievementMyPageResp();
                    p.setTaskId(v.getId());
                    p.setName(v.getName());
                    p.setTypeName(AchievementEnums.TaskType.getByCode(v.getType()));
                    p.setCategory(v.getCategory());
                   // AchievementEnums.Category en = AchievementEnums.Category.getByCode(v.getCategory());
//                    if (en != null) {
//                        p.setCategoryName(en.getName());
//                        p.setIcon(en.getIcon());
//                        p.setColor(en.getColor());
//                    }
                    p.setIcon(v.getIcon());
                    p.setColor(v.getColor());
                    p.setDescription(v.getDescription());
                    p.setTargetValue(v.getTargetValue());
                    p.setRewardType(v.getRewardType());
                    p.setRewardTypeName(AchievementEnums.RewardType.getByCode(v.getRewardType()));
                    if (v.getRewardType().equals(AchievementEnums.RewardType.POINTS.getCode())) {
                        p.setRewardValue("+" + v.getRewardValue() + "积分");
                    } else if (v.getRewardType().equals(AchievementEnums.RewardType.MEDAL.getCode())) {
                        p.setRewardValue("获得" + v.getRewardValue() + "勋章");
                    } else {
                        p.setRewardValue("+" + v.getRewardValue() + "经验值");
                    }
                    p.setStartTime(v.getStartTime());
                    p.setEndTime(v.getEndTime());
                    p.setSortOrder(v.getSortOrder());
                    if (taskId2TaskInfoMap.containsKey(v.getId())) {
                        TaskUserProgress t = taskId2TaskInfoMap.get(v.getId());
                        p.setMyTargetValue(t.getCurrentValue());
                        p.setStatusOfUserTask(t.getStatus());
                        p.setRewardClaimed(t.getRewardClaimed());
                    }
                    return p;
                }).sorted((o1, o2) -> o2.getSortOrder() - o1.getSortOrder())
                .collect(Collectors.toList());

        Page<AchievementMyPageResp> result = Page.of(req.getPage(), req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public AchievementBaseInfoResp getMyAchievementBaseInfo(Long userId) {
        long points = 0L;
        long todayPoints = 0L;
        long monthPoints = 0L;
        /**
         * 获取用户积分
         */
        List<TaskUserProgress> taskOfUserCompleted = taskUserProgressService.selectByUserId(userId, AchievementEnums.ProgressStatus.COMPLETED.getCode());
        if (!CollectionUtils.isEmpty(taskOfUserCompleted)) {
            List<Long> taskIds = taskOfUserCompleted.stream().map(v -> v.getTaskId()).distinct().collect(Collectors.toList());
            List<TaskDefinition> task = taskDefinitionService.selectByIds(taskIds);
            Map<Long, TaskDefinition> taskId2TaskInfoMap = task.stream().filter(v -> v.getRewardType().equals(AchievementEnums.RewardType.POINTS.getCode()))
                    .collect(Collectors.toMap(v -> v.getId(), v -> v));

            //计算积分
            if (!CollectionUtils.isEmpty(taskId2TaskInfoMap)) {
                //计算总积分
                points = taskId2TaskInfoMap.values().stream().mapToLong(v -> Integer.valueOf(v.getRewardValue())).sum();

                //计算今日积分
                todayPoints = taskOfUserCompleted.stream()
                        .filter(v -> taskId2TaskInfoMap.containsKey(v.getTaskId()))
                        .filter(v -> v.getLastProgressTime() != null && v.getLastProgressTime().getTime() > System.currentTimeMillis() - 24 * 60 * 60 * 1000)
                        .mapToLong(v -> v.getCurrentValue()).sum();

                //计算本月积分
                monthPoints = taskOfUserCompleted.stream()
                        .filter(v -> taskId2TaskInfoMap.containsKey(v.getTaskId()))
                        .filter(v -> v.getLastProgressTime() != null && v.getLastProgressTime().getTime() > System.currentTimeMillis() - 24 * 60 * 60 * 1000 * 30)
                        .mapToLong(v -> v.getCurrentValue()).sum();
            }
        }

        List<TaskBadgeResp> badgeResps = new ArrayList<>();
        List<TaskUserBadge> badges = taskUserBadgeService.selectByUserId(userId);
        if (!CollectionUtils.isEmpty(badges)) {
            Map<Long, TaskUserBadge> badgeId2UserBadgeInfoMap = badges.stream().collect(Collectors.toMap(v -> v.getBadgeId(), v -> v));
            List<TaskBadge> bList = taskBadgeService.selectByIds(LettuceLists.newList(badgeId2UserBadgeInfoMap.keySet()));
            badgeResps = bList.stream().map(v -> {
                        TaskBadgeResp b = new TaskBadgeResp();
                        b.setName(v.getName());
                        b.setDescription(v.getDescription());
                        b.setColorCode(v.getColorCode());
                        b.setSortOrder(v.getSortOrder());
                        b.setAchievedAt(badgeId2UserBadgeInfoMap.get(v.getId()).getAchievedAt());

                        // 根据徽章关联的任务类型设置图标URL
                        TaskBadge taskBadge = taskBadgeService.getById(v.getId());
                        if (taskBadge != null) {
                            TaskDefinition taskDefinition = taskDefinitionService.getById(taskBadge.getTaskId());
                            if (taskDefinition != null) {
                                String taskType = "";
                                if (taskDefinition.getType().equals(AchievementEnums.TaskType.DAILY.getCode())) {
                                    taskType = "DAILY";
                                } else if (taskDefinition.getType().equals(AchievementEnums.TaskType.WEEKLY.getCode())) {
                                    taskType = "WEEKLY";
                                } else if (taskDefinition.getType().equals(AchievementEnums.TaskType.ONETIME.getCode())) {
                                    taskType = "ONETIME";
                                }
                                // 假设徽章图片名称与徽章名称相关，可以根据实际情况调整
                                String iconUrl = CommonConstant.PROJECT_URL + "/imgs/" + taskType + "/" + v.getIconUrl();
                                b.setIconUrl(iconUrl);
                            }
                        }

                        return b;
                    })
                    .sorted((o1, o2) -> o2.getSortOrder() - o1.getSortOrder())
                    .collect(Collectors.toList());
        }

        AchievementBaseInfoResp resp = new AchievementBaseInfoResp();
        resp.setPoints(points);
        resp.setTodayPoints(todayPoints);
        resp.setMonthPoints(monthPoints);
        resp.setBadges(badgeResps);
        return resp;
    }

    public Boolean receiverTaskReward(Long userId, Long taskId) {
        TaskDefinition task = taskDefinitionService.selectById(taskId);
        if (task == null) {
            log.error("AchievementController#receiverTaskReward,taskId:{}不存在", taskId);
            return false;
        }
        TaskProgressQuery query = new TaskProgressQuery(task);
        query.setUserId(userId);
        query.setTaskId(taskId);
        TaskUserProgress taskUser = taskUserProgressService.selectByQuery(query);
        taskUser.setRewardClaimed(AchievementEnums.RewardStatus.CLAIMED.getCode());
        boolean result = taskUserProgressService.updateById(taskUser);
        if (result) {
            switch (task.getRewardType()) {
                case 1: // 积分
                    log.info("用户 {} 获得积分 {}", userId, task.getId());
                    addPoints(userId, Integer.valueOf(task.getRewardValue()));
                    break;
                case 2: // 勋章
                    log.info("用户 {} 获得勋章 {}", userId, task.getId());
                    TaskBadge taskBadge = taskBadgeService.selectByTaskId(task.getId());
                    awardBadge(userId, taskBadge.getId());
                    break;
                case 3: // 经验值
                    log.info("用户 {} 获得经验值 {}", userId, task.getId());
                    addExperience(userId, task.getId());
                    break;
            }
        }
        return true;
    }


    public void addPoints(Long userId, Integer rewardValue) {
        TaskUserPoint e = taskUserPointService.selectByUserId(userId);
        if (e == null) {
            e = new TaskUserPoint();
            e.setUserId(userId);
            e.setPoints(rewardValue);
            e.setCreatedAt(new Date());
            e.setCreatedBy(userId);
            e.setModifiedAt(new Date());
            e.setModifiedBy(userId);
            e.setIsDeleted(0);
            taskUserPointService.save(e);
        } else {
            e.setPoints(e.getPoints() + rewardValue);
            taskUserPointService.updateById(e);
        }
    }

    public Boolean awardBadge(Long userId, Long badgeId) {
        TaskUserBadge e = new TaskUserBadge();
        e.setUserId(userId);
        e.setBadgeId(badgeId);
        e.setAchievedAt(new Date());
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        e.setIsDeleted(0);
        return taskUserBadgeService.save(e);
    }

    public void addExperience(Long userId, Long taskId) {
    }
}
