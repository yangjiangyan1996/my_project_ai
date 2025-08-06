package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.AccountShow;
import com.example.entity.dto.Messages;
import com.example.entity.dto.TaskDefinition;
import com.example.entity.dto.TaskUserProgress;
import com.example.entity.req.AchievementMyPageReq;
import com.example.entity.resp.AchievementMyPageResp;
import com.example.entity.resp.MsgOfFollowerResp;
import com.example.enums.AchievementEnums;
import com.example.service.TaskDefinitionService;
import com.example.service.TaskUserProgressService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
public class TaskFacade {

    @Resource
    TaskUserProgressService taskUserProgressService;
    @Resource
    TaskDefinitionService taskDefinitionService;


    public void addPoints(Long userId, Long rewardValue) {
        
    }

    public void awardBadge(Long userId, Long taskId) {
        
    }

    public void addExperience(Long userId, Long rewardValue) {
    }

    public Page<AchievementMyPageResp> myAchievementPageList(AchievementMyPageReq req) {
        Page<TaskDefinition> list = taskDefinitionService.myAchievementPageList(Page.of(req.getPage() - 1, req.getSize()), req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }

        List<TaskUserProgress> myTaskList = taskUserProgressService.selectByUserId(req.getUserId());
        Map<Long, TaskUserProgress> taskId2TaskInfoMap = myTaskList.stream().collect(Collectors.toMap(v -> v.getTaskId(), v -> v));

        List<AchievementMyPageResp> collect = list.getRecords().stream().map(v -> {
                    AchievementMyPageResp p = new AchievementMyPageResp();
                    p.setTaskId(v.getId());
                    p.setName(v.getName());
                    p.setTypeName(AchievementEnums.TaskType.getByCode(v.getType()).getName());
                    p.setCategoryName(AchievementEnums.Category.getByCode(v.getCategory()).getName());
                    p.setDescription(v.getDescription());
                    p.setTargetValue(v.getTargetValue());
                    p.setRewardTypeName(AchievementEnums.RewardType.getByCode(v.getRewardType()).getName());
                    p.setRewardValue(v.getRewardValue());
                    p.setStartTime(v.getStartTime());
                    p.setEndTime(v.getEndTime());
                    p.setIcon(v.getIcon());
                    p.setSortOrder(v.getSortOrder());
                    if (taskId2TaskInfoMap.containsKey(v.getId())) {
                        TaskUserProgress t = taskId2TaskInfoMap.get(v.getId());
                        p.setMyTargetValue(t.getCurrentValue());
                    }
                    return p;
                }).sorted((o1, o2) -> o2.getSortOrder() - o1.getSortOrder())
                .collect(Collectors.toList());

        Page<AchievementMyPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }
}
