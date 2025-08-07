package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/6 15:19
 */
@Data
public class AchievementMyPageResp {
    private Long taskId; // 任务ID
    private String name; // 任务名称
    private String typeName; // 任务类型（日常/每周/一次性）
    private String category; // 分类，login / like / comment 等
    private String categoryName; // 分类，login / like / comment 等
    private String description; // 任务描述
    private Integer targetValue; // 目标数值（如5次评论）
    private String rewardTypeName; // 积分 / 勋章 / 经验值
    private String rewardValue; // 奖励值
    private Date startTime; // 任务开始时间
    private Date endTime; // 任务结束时间
    private String icon;//图标
    private String color;
    private Integer sortOrder;//排序顺序
    private Integer myTargetValue;// 我完成的目标数值（如5次评论）
}
