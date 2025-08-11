package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description 任务定义表实体类
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:36
 */
@Data
@TableName("task_definition")
public class TaskDefinition extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id; // 任务ID
    private Integer status; // 状态 0=启用  1=未启用
    private String name; // 任务名称
    private Integer type; // 任务类型（日常/每周/一次性）
    private String category; // 分类，login / like / comment 等
    private String description; // 任务描述
    private Integer targetValue; // 目标数值（如5次评论）
    private Integer rewardType; // 积分 / 勋章 / 经验值
    private String rewardValue; // 奖励值
    private Date startTime; // 任务开始时间
    private Date endTime; // 任务结束时间
    private String icon;
    private String color;
    private Integer sortOrder;
}