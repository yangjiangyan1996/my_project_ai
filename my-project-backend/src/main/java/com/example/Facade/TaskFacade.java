package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.TaskDefinition;
import com.example.entity.dto.TaskUserProgress;
import com.example.entity.query.TaskProgressQuery;
import com.example.entity.req.AchievementMyPageReq;
import com.example.entity.resp.AchievementBaseInfoResp;
import com.example.entity.resp.AchievementMyPageResp;
import com.example.enums.AchievementEnums;
import com.example.service.TaskDefinitionService;
import com.example.service.TaskUserProgressService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/6 10:00
 */
@Service
@Slf4j
public class TaskFacade {

    @Resource
    TaskUserProgressService taskUserProgressService;
    @Resource
    TaskDefinitionService taskDefinitionService;


    public void addPoints(Long userId, String rewardValue) {

    }

    public void awardBadge(Long userId, Long taskId) {

    }

    public void addExperience(Long userId, String rewardValue) {
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
                    AchievementEnums.Category en = AchievementEnums.Category.getByCode(v.getCategory());
                    if (en != null) {
                        p.setCategoryName(en.getName());
                        p.setIcon(en.getIcon());
                        p.setColor(en.getColor());
                    }
                    p.setDescription(v.getDescription());
                    p.setTargetValue(v.getTargetValue());
                    p.setRewardType(v.getRewardType());
                    p.setRewardTypeName(AchievementEnums.RewardType.getByCode(v.getRewardType()));
                    if (v.getRewardType().equals(AchievementEnums.RewardType.POINTS.getCode())) {
                        p.setRewardValue( "+" + v.getRewardValue() + "积分");
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
        List<String> medals = new ArrayList<>();
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

            //计算勋章
            medals = task.stream()
                    .filter(v -> v.getRewardType().equals(AchievementEnums.RewardType.MEDAL.getCode()))
                    .map(v -> v.getRewardValue())
                    .collect(Collectors.toList());

        }

        AchievementBaseInfoResp resp = new AchievementBaseInfoResp();
        resp.setPoints(points);
        resp.setTodayPoints(todayPoints);
        resp.setMonthPoints(monthPoints);
        resp.setBadges(medals);
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
        return taskUserProgressService.updateById(taskUser);
    }
}
